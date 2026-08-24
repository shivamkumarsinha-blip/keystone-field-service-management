package com.zidio.keystone.dto;

import com.zidio.keystone.enums.WorkOrderStatus;
import jakarta.validation.constraints.NotNull;

public record StatusChangeRequest(
        @NotNull WorkOrderStatus newStatus,
        String note
) {}
