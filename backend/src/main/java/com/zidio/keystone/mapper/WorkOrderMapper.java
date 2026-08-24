package com.zidio.keystone.mapper;

import com.zidio.keystone.config.SlaProperties;
import com.zidio.keystone.dto.WorkOrderDto;
import com.zidio.keystone.dto.WorkOrderStatusHistoryDto;
import com.zidio.keystone.entity.User;
import com.zidio.keystone.entity.WorkOrder;
import com.zidio.keystone.entity.WorkOrderStatusHistory;
import com.zidio.keystone.enums.SlaState;
import com.zidio.keystone.enums.WorkOrderStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class WorkOrderMapper {

    private final SlaProperties slaProperties;

    public WorkOrderMapper(SlaProperties slaProperties) {
        this.slaProperties = slaProperties;
    }

    public WorkOrderDto toDto(WorkOrder w) {
        if (w == null) return null;
        User tech = w.getAssignedTechnician();
        return new WorkOrderDto(
                w.getId(),
                w.getCode(),
                w.getTitle(),
                w.getDescription(),
                w.getPriority(),
                w.getStatus(),
                w.getCustomer().getId(),
                w.getCustomer().getName(),
                w.getSite().getId(),
                w.getSite().getName(),
                tech != null ? tech.getId() : null,
                tech != null ? tech.getFullName() : null,
                w.getSlaDueAt(),
                computeSlaState(w),
                w.getCreatedAt(),
                w.getUpdatedAt(),
                w.getStartedAt(),
                w.getCompletedAt(),
                w.getClosedAt()
        );
    }

    public WorkOrderStatusHistoryDto toHistoryDto(WorkOrderStatusHistory h) {
        return new WorkOrderStatusHistoryDto(
                h.getId(),
                h.getPreviousStatus(),
                h.getNewStatus(),
                h.getChangedBy() != null ? h.getChangedBy().getFullName() : "system",
                h.getNote(),
                h.getChangedAt()
        );
    }

    /** SLA state is derived, never stored, so it's always accurate against "now". */
    public SlaState computeSlaState(WorkOrder w) {
        if (w.getSlaDueAt() == null
                || w.getStatus() == WorkOrderStatus.CLOSED
                || w.getStatus() == WorkOrderStatus.CANCELLED) {
            return SlaState.OK;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(w.getSlaDueAt())) {
            return SlaState.BREACHED;
        }
        long totalMinutes = ChronoUnit.MINUTES.between(w.getCreatedAt(), w.getSlaDueAt());
        long elapsedMinutes = ChronoUnit.MINUTES.between(w.getCreatedAt(), now);
        if (totalMinutes <= 0) {
            return SlaState.OK;
        }
        double percentElapsed = (elapsedMinutes * 100.0) / totalMinutes;
        return percentElapsed >= slaProperties.getAtRiskThresholdPercent() ? SlaState.AT_RISK : SlaState.OK;
    }
}
