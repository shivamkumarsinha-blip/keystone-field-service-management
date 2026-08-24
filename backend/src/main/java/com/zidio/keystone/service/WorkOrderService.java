package com.zidio.keystone.service;

import com.zidio.keystone.config.SlaProperties;
import com.zidio.keystone.dto.*;
import com.zidio.keystone.entity.*;
import com.zidio.keystone.enums.Priority;
import com.zidio.keystone.enums.Role;
import com.zidio.keystone.enums.WorkOrderStatus;
import com.zidio.keystone.exception.BadRequestException;
import com.zidio.keystone.exception.ForbiddenException;
import com.zidio.keystone.exception.InsufficientStockException;
import com.zidio.keystone.exception.ResourceNotFoundException;
import com.zidio.keystone.mapper.WorkOrderMapper;
import com.zidio.keystone.repository.*;
import com.zidio.keystone.security.SecurityUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Owns the full work-order lifecycle: creation, dispatch, status transitions (delegated to
 * WorkOrderStateMachine), parts consumption, and time logging. Every mutating method here
 * validates role AND, where relevant, resource ownership on the backend — the frontend hiding
 * a button is never treated as a security boundary.
 */
@Service
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderStatusHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final PartRepository partRepository;
    private final PartUsageRepository partUsageRepository;
    private final TimeLogRepository timeLogRepository;
    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderStateMachine stateMachine;
    private final WorkOrderCodeGenerator codeGenerator;
    private final CustomerService customerService;
    private final SiteService siteService;
    private final SlaProperties slaProperties;

    public WorkOrderService(WorkOrderRepository workOrderRepository,
                             WorkOrderStatusHistoryRepository historyRepository,
                             UserRepository userRepository,
                             PartRepository partRepository,
                             PartUsageRepository partUsageRepository,
                             TimeLogRepository timeLogRepository,
                             WorkOrderMapper workOrderMapper,
                             WorkOrderStateMachine stateMachine,
                             WorkOrderCodeGenerator codeGenerator,
                             CustomerService customerService,
                             SiteService siteService,
                             SlaProperties slaProperties) {
        this.workOrderRepository = workOrderRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.partRepository = partRepository;
        this.partUsageRepository = partUsageRepository;
        this.timeLogRepository = timeLogRepository;
        this.workOrderMapper = workOrderMapper;
        this.stateMachine = stateMachine;
        this.codeGenerator = codeGenerator;
        this.customerService = customerService;
        this.siteService = siteService;
        this.slaProperties = slaProperties;
    }

    // ---------- Creation ----------

    @PreAuthorize("hasAnyRole('DISPATCHER','MANAGER','CUSTOMER')")
    @Transactional
    public WorkOrderDto create(WorkOrderCreateRequest request) {
        User actor = SecurityUtils.currentUser();
        Customer customer = customerService.findEntity(request.customerId());
        Site site = siteService.findEntity(request.siteId());

        if (!site.getCustomer().getId().equals(customer.getId())) {
            throw new BadRequestException("Site does not belong to the specified customer");
        }

        // A CUSTOMER may only raise a request against their own organization's site.
        if (actor.getRole() == Role.CUSTOMER) {
            boolean ownsCustomer = customer.getPortalUser() != null && customer.getPortalUser().getId().equals(actor.getId());
            if (!ownsCustomer) {
                throw new ForbiddenException("You may only raise requests for your own organization");
            }
        }

        WorkOrder workOrder = WorkOrder.builder()
                .code(codeGenerator.nextCode())
                .title(request.title())
                .description(request.description())
                .priority(request.priority())
                .status(WorkOrderStatus.NEW)
                .customer(customer)
                .site(site)
                .createdBy(actor)
                .slaDueAt(LocalDateTime.now().plusHours(slaHoursFor(request.priority())))
                .build();
        workOrder = workOrderRepository.save(workOrder);

        recordHistory(workOrder, null, WorkOrderStatus.NEW, actor, "Work order created");
        return workOrderMapper.toDto(workOrder);
    }

    private long slaHoursFor(Priority priority) {
        return switch (priority) {
            case LOW -> slaProperties.getLowHours();
            case MEDIUM -> slaProperties.getMediumHours();
            case HIGH -> slaProperties.getHighHours();
            case URGENT -> slaProperties.getUrgentHours();
        };
    }

    // ---------- Read / search (role-scoped) ----------

    /**
     * Every role sees a different slice of work orders, enforced here — not just in the UI.
     * TECHNICIAN only ever sees jobs assigned to them; CUSTOMER only ever sees their own
     * organization's work orders, regardless of what filters are passed in.
     */
    public PageResponse<WorkOrderDto> search(WorkOrderStatus status, Long technicianId, Long siteId,
                                              String q, Pageable pageable) {
        User actor = SecurityUtils.currentUser();

        Long effectiveTechnicianId = technicianId;
        Long effectiveCustomerId = null;

        if (actor.getRole() == Role.TECHNICIAN) {
            effectiveTechnicianId = actor.getId(); // technicians cannot broaden the filter to other technicians
        } else if (actor.getRole() == Role.CUSTOMER) {
            effectiveCustomerId = requireOwnedCustomerId(actor);
        }

        var page = workOrderRepository.search(status, effectiveTechnicianId, effectiveCustomerId, siteId, q, pageable);
        return PageResponse.from(page.map(workOrderMapper::toDto));
    }

    public WorkOrderDto get(Long id) {
        return workOrderMapper.toDto(getScopedEntity(id));
    }

    public List<WorkOrderStatusHistoryDto> getHistory(Long id) {
        WorkOrder workOrder = getScopedEntity(id); // enforces the same ownership rules as get()
        return historyRepository.findByWorkOrder_IdOrderByChangedAtAsc(workOrder.getId())
                .stream().map(workOrderMapper::toHistoryDto).toList();
    }

    /** Fetches a work order while enforcing per-role visibility (technician/customer isolation). */
    private WorkOrder getScopedEntity(Long id) {
        User actor = SecurityUtils.currentUser();
        return switch (actor.getRole()) {
            case TECHNICIAN -> workOrderRepository.findByIdAndAssignedTechnician_Id(id, actor.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Work order " + id + " not found"));
            case CUSTOMER -> workOrderRepository.findByIdAndCustomer_Id(id, requireOwnedCustomerId(actor))
                    .orElseThrow(() -> new ResourceNotFoundException("Work order " + id + " not found"));
            case DISPATCHER, MANAGER -> workOrderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Work order " + id + " not found"));
        };
    }

    /**
     * A CUSTOMER-role user is linked to exactly one Customer organization via portal_user_id.
     * Always resolved server-side from the authenticated principal — never trusted from a
     * client-supplied id — so a customer can never widen their query to another organization.
     */
    private Long requireOwnedCustomerId(User customerUser) {
        return customerService.findByPortalUserIdOrThrow(customerUser.getId());
    }

    // ---------- Dispatch ----------

    @PreAuthorize("hasAnyRole('DISPATCHER','MANAGER')")
    @Transactional
    public WorkOrderDto assign(Long id, AssignRequest request) {
        User actor = SecurityUtils.currentUser();
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order " + id + " not found"));

        if (stateMachine.isTerminal(workOrder.getStatus())) {
            throw new BadRequestException("Cannot reassign a " + workOrder.getStatus() + " work order");
        }

        User technician = userRepository.findById(request.technicianId())
                .orElseThrow(() -> new ResourceNotFoundException("Technician " + request.technicianId() + " not found"));
        if (technician.getRole() != Role.TECHNICIAN) {
            throw new BadRequestException("User " + technician.getId() + " is not a technician");
        }

        WorkOrderStatus previousStatus = workOrder.getStatus();
        workOrder.setAssignedTechnician(technician);
        if (workOrder.getStatus() == WorkOrderStatus.NEW) {
            workOrder.setStatus(WorkOrderStatus.ASSIGNED);
        }

        recordHistory(workOrder, previousStatus, workOrder.getStatus(), actor,
                "Assigned to " + technician.getFullName());
        return workOrderMapper.toDto(workOrder);
    }

    // ---------- Status transitions ----------

    @Transactional
    public WorkOrderDto changeStatus(Long id, StatusChangeRequest request) {
        User actor = SecurityUtils.currentUser();
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order " + id + " not found"));

        WorkOrderStatus previousStatus = workOrder.getStatus();
        stateMachine.validateTransition(workOrder, request.newStatus(), actor); // throws if illegal

        workOrder.setStatus(request.newStatus());
        LocalDateTime now = LocalDateTime.now();
        switch (request.newStatus()) {
            case IN_PROGRESS -> { if (workOrder.getStartedAt() == null) workOrder.setStartedAt(now); }
            case COMPLETED -> workOrder.setCompletedAt(now);
            case CLOSED -> workOrder.setClosedAt(now);
            default -> { /* no timestamp side effect */ }
        }

        recordHistory(workOrder, previousStatus, workOrder.getStatus(), actor, request.note());
        return workOrderMapper.toDto(workOrder);
    }

    private void recordHistory(WorkOrder workOrder, WorkOrderStatus previous, WorkOrderStatus next,
                                User actor, String note) {
        WorkOrderStatusHistory history = WorkOrderStatusHistory.builder()
                .workOrder(workOrder)
                .previousStatus(previous)
                .newStatus(next)
                .changedBy(actor)
                .note(note)
                .build();
        historyRepository.save(history);
    }

    // ---------- Parts logging (single transaction, stock never goes negative) ----------

    @PreAuthorize("hasRole('TECHNICIAN')")
    @Transactional
    public WorkOrderDto logPartUsage(Long workOrderId, PartUsageRequest request) {
        User actor = SecurityUtils.currentUser();
        WorkOrder workOrder = workOrderRepository.findByIdAndAssignedTechnician_Id(workOrderId, actor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Work order " + workOrderId + " not found"));

        if (stateMachine.isTerminal(workOrder.getStatus())) {
            throw new BadRequestException("Cannot log parts on a " + workOrder.getStatus() + " work order");
        }

        Part part = partRepository.findById(request.partId())
                .orElseThrow(() -> new ResourceNotFoundException("Part " + request.partId() + " not found"));

        if (part.getQuantityInStock() < request.quantity()) {
            throw new InsufficientStockException(part.getName(), part.getQuantityInStock(), request.quantity());
        }

        // Decrement stock and record usage atomically — if either write fails, the whole
        // @Transactional method rolls back and stock is left untouched.
        part.setQuantityInStock(part.getQuantityInStock() - request.quantity());

        PartUsage usage = PartUsage.builder()
                .workOrder(workOrder)
                .part(part)
                .technician(actor)
                .quantity(request.quantity())
                .unitCostAtUse(part.getUnitCost())
                .build();
        partUsageRepository.save(usage);

        return workOrderMapper.toDto(workOrder);
    }

    // ---------- Time logging ----------

    @PreAuthorize("hasRole('TECHNICIAN')")
    @Transactional
    public WorkOrderDto logTime(Long workOrderId, TimeLogRequest request) {
        User actor = SecurityUtils.currentUser();
        WorkOrder workOrder = workOrderRepository.findByIdAndAssignedTechnician_Id(workOrderId, actor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Work order " + workOrderId + " not found"));

        TimeLog timeLog = TimeLog.builder()
                .workOrder(workOrder)
                .technician(actor)
                .minutes(request.minutes())
                .note(request.note())
                .build();
        timeLogRepository.save(timeLog);

        return workOrderMapper.toDto(workOrder);
    }

    public BigDecimal totalPartsCost(Long workOrderId) {
        return partUsageRepository.findByWorkOrder_Id(workOrderId).stream()
                .map(u -> u.getUnitCostAtUse().multiply(BigDecimal.valueOf(u.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int totalMinutesLogged(Long workOrderId) {
        return timeLogRepository.findByWorkOrder_Id(workOrderId).stream()
                .mapToInt(TimeLog::getMinutes).sum();
    }
}
