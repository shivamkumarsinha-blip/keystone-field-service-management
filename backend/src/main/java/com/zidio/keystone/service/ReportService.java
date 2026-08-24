package com.zidio.keystone.service;

import com.zidio.keystone.dto.ReportSummaryDto;
import com.zidio.keystone.entity.WorkOrder;
import com.zidio.keystone.enums.Priority;
import com.zidio.keystone.enums.SlaState;
import com.zidio.keystone.enums.WorkOrderStatus;
import com.zidio.keystone.mapper.WorkOrderMapper;
import com.zidio.keystone.repository.WorkOrderRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/** Backs the manager dashboard. Every number here is computed live from the database — nothing hard-coded. */
@Service
public class ReportService {

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderMapper workOrderMapper;

    public ReportService(WorkOrderRepository workOrderRepository, WorkOrderMapper workOrderMapper) {
        this.workOrderRepository = workOrderRepository;
        this.workOrderMapper = workOrderMapper;
    }

    @PreAuthorize("hasAnyRole('MANAGER','DISPATCHER')")
    public ReportSummaryDto summary() {
        List<WorkOrder> all = workOrderRepository.findAll(Pageable.unpaged()).getContent();

        Map<String, Long> countsByStatus = new LinkedHashMap<>();
        for (WorkOrderStatus status : WorkOrderStatus.values()) {
            countsByStatus.put(status.name(), all.stream().filter(w -> w.getStatus() == status).count());
        }

        LocalDateTime now = LocalDateTime.now();
        long overdue = all.stream()
                .filter(w -> w.getSlaDueAt() != null && w.getSlaDueAt().isBefore(now)
                        && w.getStatus() != WorkOrderStatus.CLOSED && w.getStatus() != WorkOrderStatus.CANCELLED)
                .count();

        long atRisk = all.stream()
                .filter(w -> workOrderMapper.computeSlaState(w) == SlaState.AT_RISK)
                .count();

        long highPriorityOpen = all.stream()
                .filter(w -> (w.getPriority() == Priority.HIGH || w.getPriority() == Priority.URGENT)
                        && w.getStatus() != WorkOrderStatus.CLOSED && w.getStatus() != WorkOrderStatus.CANCELLED)
                .count();

        Map<String, List<WorkOrder>> byTechnician = all.stream()
                .filter(w -> w.getAssignedTechnician() != null
                        && w.getStatus() != WorkOrderStatus.CLOSED && w.getStatus() != WorkOrderStatus.CANCELLED)
                .collect(Collectors.groupingBy(w -> w.getAssignedTechnician().getId() + ":" + w.getAssignedTechnician().getFullName()));

        List<ReportSummaryDto.TechnicianLoadDto> workByTechnician = byTechnician.entrySet().stream()
                .map(e -> {
                    String[] parts = e.getKey().split(":", 2);
                    return new ReportSummaryDto.TechnicianLoadDto(Long.valueOf(parts[0]), parts[1], e.getValue().size());
                })
                .sorted(Comparator.comparingLong(ReportSummaryDto.TechnicianLoadDto::openCount).reversed())
                .toList();

        Map<String, List<WorkOrder>> bySite = all.stream()
                .filter(w -> w.getStatus() != WorkOrderStatus.CLOSED && w.getStatus() != WorkOrderStatus.CANCELLED)
                .collect(Collectors.groupingBy(w -> w.getSite().getId() + ":" + w.getSite().getName()));

        List<ReportSummaryDto.SiteLoadDto> workBySite = bySite.entrySet().stream()
                .map(e -> {
                    String[] parts = e.getKey().split(":", 2);
                    return new ReportSummaryDto.SiteLoadDto(Long.valueOf(parts[0]), parts[1], e.getValue().size());
                })
                .sorted(Comparator.comparingLong(ReportSummaryDto.SiteLoadDto::openCount).reversed())
                .toList();

        return new ReportSummaryDto(all.size(), countsByStatus, overdue, atRisk, highPriorityOpen,
                workByTechnician, workBySite);
    }
}
