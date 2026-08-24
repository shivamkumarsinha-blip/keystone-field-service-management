package com.zidio.keystone.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String partName, int available, int requested) {
        super("Insufficient stock for '" + partName + "': available=" + available + ", requested=" + requested);
    }
}
