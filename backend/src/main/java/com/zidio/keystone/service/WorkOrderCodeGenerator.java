package com.zidio.keystone.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.time.Year;

/** Generates human-readable, unique codes like WO-2026-000001 backed by a DB sequence. */
@Component
public class WorkOrderCodeGenerator {

    @PersistenceContext
    private EntityManager entityManager;

    public String nextCode() {
        Number seqValue = (Number) entityManager
                .createNativeQuery("select nextval('work_order_code_seq')")
                .getSingleResult();
        int year = Year.now().getValue();
        return String.format("WO-%d-%06d", year, seqValue.longValue());
    }
}
