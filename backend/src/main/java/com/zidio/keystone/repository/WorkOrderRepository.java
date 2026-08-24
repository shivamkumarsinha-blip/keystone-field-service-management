package com.zidio.keystone.repository;

import com.zidio.keystone.entity.WorkOrder;
import com.zidio.keystone.enums.WorkOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    Optional<WorkOrder> findByCode(String code);

    // Customer portal isolation: customer can only ever query their OWN work orders.
    Page<WorkOrder> findByCustomer_Id(Long customerId, Pageable pageable);
    Optional<WorkOrder> findByIdAndCustomer_Id(Long id, Long customerId);

    // Technician isolation: technician can only ever query jobs assigned to THEM.
    Page<WorkOrder> findByAssignedTechnician_Id(Long technicianId, Pageable pageable);
    Optional<WorkOrder> findByIdAndAssignedTechnician_Id(Long id, Long technicianId);

    Page<WorkOrder> findByStatus(WorkOrderStatus status, Pageable pageable);

    @Query("select w from WorkOrder w where " +
           "(:status is null or w.status = :status) and " +
           "(:technicianId is null or w.assignedTechnician.id = :technicianId) and " +
           "(:customerId is null or w.customer.id = :customerId) and " +
           "(:siteId is null or w.site.id = :siteId) and " +
           "(:search is null or lower(w.title) like lower(concat('%', :search, '%')) or lower(w.code) like lower(concat('%', :search, '%')))")
    Page<WorkOrder> search(@Param("status") WorkOrderStatus status,
                            @Param("technicianId") Long technicianId,
                            @Param("customerId") Long customerId,
                            @Param("siteId") Long siteId,
                            @Param("search") String search,
                            Pageable pageable);

    List<WorkOrder> findByStatusInAndSlaDueAtBefore(List<WorkOrderStatus> statuses, LocalDateTime cutoff);

    long countByStatus(WorkOrderStatus status);

    @Query("select count(w) from WorkOrder w where w.status not in ('CLOSED','CANCELLED') and w.slaDueAt < :now")
    long countOverdue(@Param("now") LocalDateTime now);
}
