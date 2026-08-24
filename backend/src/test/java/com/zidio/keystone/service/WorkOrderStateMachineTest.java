package com.zidio.keystone.service;

import com.zidio.keystone.entity.User;
import com.zidio.keystone.entity.WorkOrder;
import com.zidio.keystone.enums.Priority;
import com.zidio.keystone.enums.Role;
import com.zidio.keystone.enums.WorkOrderStatus;
import com.zidio.keystone.exception.ForbiddenException;
import com.zidio.keystone.exception.InvalidWorkOrderTransitionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the work-order lifecycle rules. These are the rules the whole platform's
 * integrity depends on, so every legal and illegal edge from the brief is exercised here.
 */
class WorkOrderStateMachineTest {

    private WorkOrderStateMachine stateMachine;
    private User dispatcher;
    private User manager;
    private User technicianOwner;
    private User technicianOther;
    private User customer;

    @BeforeEach
    void setUp() {
        stateMachine = new WorkOrderStateMachine();
        dispatcher = User.builder().id(1L).role(Role.DISPATCHER).build();
        manager = User.builder().id(2L).role(Role.MANAGER).build();
        technicianOwner = User.builder().id(3L).role(Role.TECHNICIAN).build();
        technicianOther = User.builder().id(4L).role(Role.TECHNICIAN).build();
        customer = User.builder().id(5L).role(Role.CUSTOMER).build();
    }

    private WorkOrder workOrder(WorkOrderStatus status, User assignedTechnician) {
        return WorkOrder.builder()
                .id(100L)
                .status(status)
                .priority(Priority.HIGH)
                .assignedTechnician(assignedTechnician)
                .build();
    }

    @Test
    void dispatcherCanMoveNewToAssigned() {
        WorkOrder wo = workOrder(WorkOrderStatus.NEW, null);
        assertThatCode(() -> stateMachine.validateTransition(wo, WorkOrderStatus.ASSIGNED, dispatcher))
                .doesNotThrowAnyException();
    }

    @Test
    void technicianCannotMoveNewToAssigned() {
        WorkOrder wo = workOrder(WorkOrderStatus.NEW, technicianOwner);
        assertThatThrownBy(() -> stateMachine.validateTransition(wo, WorkOrderStatus.ASSIGNED, technicianOwner))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void assignedTechnicianCanStartTheirOwnJob() {
        WorkOrder wo = workOrder(WorkOrderStatus.ASSIGNED, technicianOwner);
        assertThatCode(() -> stateMachine.validateTransition(wo, WorkOrderStatus.IN_PROGRESS, technicianOwner))
                .doesNotThrowAnyException();
    }

    @Test
    void technicianCannotStartAnotherTechniciansJob() {
        WorkOrder wo = workOrder(WorkOrderStatus.ASSIGNED, technicianOwner);
        assertThatThrownBy(() -> stateMachine.validateTransition(wo, WorkOrderStatus.IN_PROGRESS, technicianOther))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void technicianCanHoldAndResume() {
        WorkOrder wo = workOrder(WorkOrderStatus.IN_PROGRESS, technicianOwner);
        assertThatCode(() -> stateMachine.validateTransition(wo, WorkOrderStatus.ON_HOLD, technicianOwner))
                .doesNotThrowAnyException();

        wo.setStatus(WorkOrderStatus.ON_HOLD);
        assertThatCode(() -> stateMachine.validateTransition(wo, WorkOrderStatus.IN_PROGRESS, technicianOwner))
                .doesNotThrowAnyException();
    }

    @Test
    void technicianCannotCloseAWorkOrder() {
        WorkOrder wo = workOrder(WorkOrderStatus.COMPLETED, technicianOwner);
        assertThatThrownBy(() -> stateMachine.validateTransition(wo, WorkOrderStatus.CLOSED, technicianOwner))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void managerCanCloseACompletedWorkOrder() {
        WorkOrder wo = workOrder(WorkOrderStatus.COMPLETED, technicianOwner);
        assertThatCode(() -> stateMachine.validateTransition(wo, WorkOrderStatus.CLOSED, manager))
                .doesNotThrowAnyException();
    }

    @Test
    void closedWorkOrderCannotTransitionAnywhere() {
        WorkOrder wo = workOrder(WorkOrderStatus.CLOSED, technicianOwner);
        assertThatThrownBy(() -> stateMachine.validateTransition(wo, WorkOrderStatus.IN_PROGRESS, manager))
                .isInstanceOf(InvalidWorkOrderTransitionException.class);
    }

    @Test
    void cancelledWorkOrderCannotTransitionAnywhere() {
        WorkOrder wo = workOrder(WorkOrderStatus.CANCELLED, null);
        assertThatThrownBy(() -> stateMachine.validateTransition(wo, WorkOrderStatus.ASSIGNED, dispatcher))
                .isInstanceOf(InvalidWorkOrderTransitionException.class);
    }

    @Test
    void cannotSkipStraightFromNewToInProgress() {
        WorkOrder wo = workOrder(WorkOrderStatus.NEW, null);
        assertThatThrownBy(() -> stateMachine.validateTransition(wo, WorkOrderStatus.IN_PROGRESS, manager))
                .isInstanceOf(InvalidWorkOrderTransitionException.class);
    }

    @Test
    void dispatcherCanCancelANewWorkOrder() {
        WorkOrder wo = workOrder(WorkOrderStatus.NEW, null);
        assertThatCode(() -> stateMachine.validateTransition(wo, WorkOrderStatus.CANCELLED, dispatcher))
                .doesNotThrowAnyException();
    }

    @Test
    void customerCanNeverDriveAStatusTransition() {
        WorkOrder wo = workOrder(WorkOrderStatus.NEW, null);
        assertThatThrownBy(() -> stateMachine.validateTransition(wo, WorkOrderStatus.ASSIGNED, customer))
                .isInstanceOf(ForbiddenException.class);
    }
}
