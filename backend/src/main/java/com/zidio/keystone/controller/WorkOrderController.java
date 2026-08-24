package com.zidio.keystone.controller;

import com.zidio.keystone.dto.*;
import com.zidio.keystone.enums.WorkOrderStatus;
import com.zidio.keystone.service.WorkOrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work-orders")
@Tag(name = "Work Orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @GetMapping
    public PageResponse<WorkOrderDto> search(@RequestParam(required = false) WorkOrderStatus status,
                                              @RequestParam(required = false) Long technicianId,
                                              @RequestParam(required = false) Long siteId,
                                              @RequestParam(required = false) String q,
                                              Pageable pageable) {
        return workOrderService.search(status, technicianId, siteId, q, pageable);
    }

    @PostMapping
    public ResponseEntity<WorkOrderDto> create(@Valid @RequestBody WorkOrderCreateRequest request) {
        return ResponseEntity.status(201).body(workOrderService.create(request));
    }

    @GetMapping("/{id}")
    public WorkOrderDto get(@PathVariable Long id) {
        return workOrderService.get(id);
    }

    @GetMapping("/{id}/history")
    public List<WorkOrderStatusHistoryDto> history(@PathVariable Long id) {
        return workOrderService.getHistory(id);
    }

    @PostMapping("/{id}/assign")
    public WorkOrderDto assign(@PathVariable Long id, @Valid @RequestBody AssignRequest request) {
        return workOrderService.assign(id, request);
    }

    @PostMapping("/{id}/status")
    public WorkOrderDto changeStatus(@PathVariable Long id, @Valid @RequestBody StatusChangeRequest request) {
        return workOrderService.changeStatus(id, request);
    }

    @PostMapping("/{id}/parts")
    public WorkOrderDto logParts(@PathVariable Long id, @Valid @RequestBody PartUsageRequest request) {
        return workOrderService.logPartUsage(id, request);
    }

    @PostMapping("/{id}/time")
    public WorkOrderDto logTime(@PathVariable Long id, @Valid @RequestBody TimeLogRequest request) {
        return workOrderService.logTime(id, request);
    }
}
