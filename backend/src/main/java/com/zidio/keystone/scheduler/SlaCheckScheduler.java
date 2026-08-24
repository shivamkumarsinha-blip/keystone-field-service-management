package com.zidio.keystone.scheduler;

import com.zidio.keystone.entity.WorkOrder;
import com.zidio.keystone.enums.WorkOrderStatus;
import com.zidio.keystone.repository.WorkOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Periodically scans open work orders for SLA breaches. This is intentionally a simple,
 * maintainable notification abstraction (logging today) — swap logBreach()'s body for an
 * email/Slack/webhook call without touching the scan logic.
 */
@Component
public class SlaCheckScheduler {

    private static final Logger log = LoggerFactory.getLogger(SlaCheckScheduler.class);

    private final WorkOrderRepository workOrderRepository;

    public SlaCheckScheduler(WorkOrderRepository workOrderRepository) {
        this.workOrderRepository = workOrderRepository;
    }

    private static final List<WorkOrderStatus> OPEN_STATUSES =
            List.of(WorkOrderStatus.NEW, WorkOrderStatus.ASSIGNED, WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.ON_HOLD);

    /** Runs every 5 minutes; interval is intentionally short for local/dev visibility. */
    @Scheduled(fixedRateString = "${keystone.sla.check-interval-ms:300000}")
    public void checkForBreaches() {
        List<WorkOrder> breached = workOrderRepository.findByStatusInAndSlaDueAtBefore(OPEN_STATUSES, LocalDateTime.now());
        for (WorkOrder wo : breached) {
            notifyBreach(wo);
        }
    }

    private void notifyBreach(WorkOrder wo) {
        log.warn("SLA BREACHED: work order {} ({}) is past its SLA due date of {}",
                wo.getCode(), wo.getTitle(), wo.getSlaDueAt());
        // Extension point: publish an in-app notification / send an email or Slack alert here.
    }
}
