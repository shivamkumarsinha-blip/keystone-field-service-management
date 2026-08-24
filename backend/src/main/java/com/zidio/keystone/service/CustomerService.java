package com.zidio.keystone.service;

import com.zidio.keystone.dto.CustomerCreateRequest;
import com.zidio.keystone.dto.CustomerDto;
import com.zidio.keystone.dto.PageResponse;
import com.zidio.keystone.entity.Customer;
import com.zidio.keystone.entity.User;
import com.zidio.keystone.exception.ResourceNotFoundException;
import com.zidio.keystone.mapper.CustomerMapper;
import com.zidio.keystone.repository.CustomerRepository;
import com.zidio.keystone.security.SecurityUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerService(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @PreAuthorize("hasAnyRole('DISPATCHER','MANAGER')")
    public PageResponse<CustomerDto> list(String search, Pageable pageable) {
        var page = (search == null || search.isBlank())
                ? customerRepository.findAll(pageable)
                : customerRepository.findByNameContainingIgnoreCase(search, pageable);
        return PageResponse.from(page.map(customerMapper::toDto));
    }

    @PreAuthorize("hasAnyRole('DISPATCHER','MANAGER')")
    public CustomerDto get(Long id) {
        return customerMapper.toDto(findEntity(id));
    }

    @PreAuthorize("hasAnyRole('DISPATCHER','MANAGER')")
    @Transactional
    public CustomerDto create(CustomerCreateRequest request) {
        Customer customer = Customer.builder()
                .name(request.name())
                .contactEmail(request.contactEmail())
                .contactPhone(request.contactPhone())
                .active(true)
                .build();
        return customerMapper.toDto(customerRepository.save(customer));
    }

    @PreAuthorize("hasAnyRole('DISPATCHER','MANAGER')")
    @Transactional
    public CustomerDto update(Long id, CustomerCreateRequest request) {
        Customer customer = findEntity(id);
        customer.setName(request.name());
        customer.setContactEmail(request.contactEmail());
        customer.setContactPhone(request.contactPhone());
        return customerMapper.toDto(customer);
    }

    Customer findEntity(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer " + id + " not found"));
    }

    /** Used by WorkOrderService to resolve a CUSTOMER-role user's own organization id for data isolation. */
    Long findByPortalUserIdOrThrow(Long portalUserId) {
        return customerRepository.findByPortalUser_Id(portalUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No customer organization linked to this account"))
                .getId();
    }

    /**
     * Resolves the CUSTOMER-role caller's own organization, purely from their authenticated
     * identity (never from a client-supplied id). Backs GET /api/customers/me so the portal
     * frontend never has to guess or cache a customerId itself.
     */
    @PreAuthorize("hasRole('CUSTOMER')")
    public CustomerDto getMine() {
        User actor = SecurityUtils.currentUser();
        Long customerId = findByPortalUserIdOrThrow(actor.getId());
        return customerMapper.toDto(findEntity(customerId));
    }
}
