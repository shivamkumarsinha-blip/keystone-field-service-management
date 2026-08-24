package com.zidio.keystone.dto;

import com.zidio.keystone.enums.WorkOrderStatus;

import java.time.LocalDateTime;

public record WorkOrderStatusHistoryDto(
        Long id,
        WorkOrderStatus previousStatus,
        WorkOrderStatus newStatus,
        String changedByName,
        String note,
        LocalDateTime changedAt
) {}
