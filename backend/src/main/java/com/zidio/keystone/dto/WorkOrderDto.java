package com.zidio.keystone.dto;

import com.zidio.keystone.enums.Priority;
import com.zidio.keystone.enums.SlaState;
import com.zidio.keystone.enums.WorkOrderStatus;

import java.time.LocalDateTime;

public record WorkOrderDto(
        Long id,
        String code,
        String title,
        String description,
        Priority priority,
        WorkOrderStatus status,
        Long customerId,
        String customerName,
        Long siteId,
        String siteName,
        Long assignedTechnicianId,
        String assignedTechnicianName,
        LocalDateTime slaDueAt,
        SlaState slaState,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime closedAt
) {}
