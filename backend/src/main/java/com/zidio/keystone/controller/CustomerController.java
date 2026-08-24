package com.zidio.keystone.controller;

import com.zidio.keystone.dto.CustomerCreateRequest;
import com.zidio.keystone.dto.CustomerDto;
import com.zidio.keystone.dto.PageResponse;
import com.zidio.keystone.dto.SiteCreateRequest;
import com.zidio.keystone.dto.SiteDto;
import com.zidio.keystone.service.CustomerService;
import com.zidio.keystone.service.SiteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customers")
public class CustomerController {

    private final CustomerService customerService;
    private final SiteService siteService;

    public CustomerController(CustomerService customerService, SiteService siteService) {
        this.customerService = customerService;
        this.siteService = siteService;
    }

    @GetMapping
    public PageResponse<CustomerDto> list(@RequestParam(required = false) String search, Pageable pageable) {
        return customerService.list(search, pageable);
    }

    @PostMapping
    public ResponseEntity<CustomerDto> create(@Valid @RequestBody CustomerCreateRequest request) {
        return ResponseEntity.status(201).body(customerService.create(request));
    }

    @GetMapping("/me")
    public CustomerDto getMine() {
        return customerService.getMine();
    }

    @GetMapping("/me/sites")
    public PageResponse<SiteDto> listMySites(Pageable pageable) {
        return siteService.listMySites(pageable);
    }

    @GetMapping("/{id}")
    public CustomerDto get(@PathVariable Long id) {
        return customerService.get(id);
    }

    @PutMapping("/{id}")
    public CustomerDto update(@PathVariable Long id, @Valid @RequestBody CustomerCreateRequest request) {
        return customerService.update(id, request);
    }

    @GetMapping("/{id}/sites")
    public PageResponse<SiteDto> listSites(@PathVariable Long id, Pageable pageable) {
        return siteService.listByCustomer(id, pageable);
    }

    @PostMapping("/{id}/sites")
    public ResponseEntity<SiteDto> createSite(@PathVariable Long id, @Valid @RequestBody SiteCreateRequest request) {
        return ResponseEntity.status(201).body(siteService.create(id, request));
    }
}
