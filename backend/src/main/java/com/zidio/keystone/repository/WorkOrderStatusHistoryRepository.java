package com.zidio.keystone.repository;

import com.zidio.keystone.entity.WorkOrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkOrderStatusHistoryRepository extends JpaRepository<WorkOrderStatusHistory, Long> {
    List<WorkOrderStatusHistory> findByWorkOrder_IdOrderByChangedAtAsc(Long workOrderId);
}
