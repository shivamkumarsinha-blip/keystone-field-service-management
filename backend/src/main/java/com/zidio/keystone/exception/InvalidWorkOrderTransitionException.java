package com.zidio.keystone.exception;

import com.zidio.keystone.enums.WorkOrderStatus;

public class InvalidWorkOrderTransitionException extends RuntimeException {
    public InvalidWorkOrderTransitionException(WorkOrderStatus from, WorkOrderStatus to) {
        super("Cannot transition work order from " + from + " to " + to);
    }

    public InvalidWorkOrderTransitionException(String message) {
        super(message);
    }
}
