package com.zidio.keystone.service;

import com.zidio.keystone.entity.User;
import com.zidio.keystone.entity.WorkOrder;
import com.zidio.keystone.enums.Role;
import com.zidio.keystone.enums.WorkOrderStatus;
import com.zidio.keystone.exception.ForbiddenException;
import com.zidio.keystone.exception.InvalidWorkOrderTransitionException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.zidio.keystone.enums.WorkOrderStatus.*;

/**
 * The single source of truth for legal work-order status transitions and who may perform them.
 * No other part of the codebase should decide whether a transition is allowed — everything
 * routes through validateTransition() so the rules can never drift out of sync between
 * controllers, services, or the frontend.
 */
@Component
public class WorkOrderStateMachine {

    // from-status -> set of legal to-statuses
    private static final Map<WorkOrderStatus, Set<WorkOrderStatus>> TRANSITIONS = new EnumMap<>(WorkOrderStatus.class);

    // "from -> to" pair -> roles allowed to perform it (in addition to any ownership checks below)
    private static final Map<String, Set<Role>> TRANSITION_ROLES = new java.util.HashMap<>();

    static {
        TRANSITIONS.put(NEW, EnumSet.of(ASSIGNED, CANCELLED));
        TRANSITIONS.put(ASSIGNED, EnumSet.of(IN_PROGRESS, CANCELLED));
        TRANSITIONS.put(IN_PROGRESS, EnumSet.of(ON_HOLD, COMPLETED));
        TRANSITIONS.put(ON_HOLD, EnumSet.of(IN_PROGRESS));
        TRANSITIONS.put(COMPLETED, EnumSet.of(CLOSED));
        TRANSITIONS.put(CLOSED, EnumSet.noneOf(WorkOrderStatus.class));
        TRANSITIONS.put(CANCELLED, EnumSet.noneOf(WorkOrderStatus.class));

        rule(NEW, ASSIGNED, Role.DISPATCHER, Role.MANAGER);
        rule(NEW, CANCELLED, Role.DISPATCHER, Role.MANAGER);
        rule(ASSIGNED, IN_PROGRESS, Role.TECHNICIAN, Role.MANAGER);
        rule(ASSIGNED, CANCELLED, Role.DISPATCHER, Role.MANAGER);
        rule(IN_PROGRESS, ON_HOLD, Role.TECHNICIAN, Role.MANAGER);
        rule(IN_PROGRESS, COMPLETED, Role.TECHNICIAN, Role.MANAGER);
        rule(ON_HOLD, IN_PROGRESS, Role.TECHNICIAN, Role.MANAGER);
        rule(COMPLETED, CLOSED, Role.MANAGER);
    }

    private static void rule(WorkOrderStatus from, WorkOrderStatus to, Role... roles) {
        TRANSITION_ROLES.put(key(from, to), EnumSet.copyOf(Set.of(roles)));
    }

    private static String key(WorkOrderStatus from, WorkOrderStatus to) {
        return from.name() + "->" + to.name();
    }

    /**
     * Validates a requested transition against the state machine, the acting user's role, and
     * (for technicians) ownership of the work order. Throws if the transition is illegal;
     * callers should let that exception propagate to a 409/403 response.
     */
    public void validateTransition(WorkOrder workOrder, WorkOrderStatus newStatus, User actor) {
        WorkOrderStatus current = workOrder.getStatus();

        Set<WorkOrderStatus> allowedTargets = TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowedTargets.contains(newStatus)) {
            throw new InvalidWorkOrderTransitionException(current, newStatus);
        }

        Set<Role> allowedRoles = TRANSITION_ROLES.getOrDefault(key(current, newStatus), Set.of());
        if (!allowedRoles.contains(actor.getRole())) {
            throw new ForbiddenException(
                    "Role " + actor.getRole() + " is not permitted to move a work order from " + current + " to " + newStatus);
        }

        if (actor.getRole() == Role.TECHNICIAN) {
            boolean isOwner = workOrder.getAssignedTechnician() != null
                    && workOrder.getAssignedTechnician().getId().equals(actor.getId());
            if (!isOwner) {
                throw new ForbiddenException("Technicians may only update work orders assigned to them");
            }
        }
    }

    public boolean isTerminal(WorkOrderStatus status) {
        return status == CLOSED || status == CANCELLED;
    }
}
