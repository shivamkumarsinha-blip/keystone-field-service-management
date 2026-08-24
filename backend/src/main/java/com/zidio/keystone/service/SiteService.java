package com.zidio.keystone.service;

import com.zidio.keystone.dto.PageResponse;
import com.zidio.keystone.dto.SiteCreateRequest;
import com.zidio.keystone.dto.SiteDto;
import com.zidio.keystone.entity.Customer;
import com.zidio.keystone.entity.Site;
import com.zidio.keystone.exception.ResourceNotFoundException;
import com.zidio.keystone.mapper.SiteMapper;
import com.zidio.keystone.repository.SiteRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SiteService {

    private final SiteRepository siteRepository;
    private final SiteMapper siteMapper;
    private final CustomerService customerService;

    public SiteService(SiteRepository siteRepository, SiteMapper siteMapper, CustomerService customerService) {
        this.siteRepository = siteRepository;
        this.siteMapper = siteMapper;
        this.customerService = customerService;
    }

    @PreAuthorize("hasAnyRole('DISPATCHER','MANAGER')")
    public PageResponse<SiteDto> listByCustomer(Long customerId, Pageable pageable) {
        return PageResponse.from(siteRepository.findByCustomer_Id(customerId, pageable).map(siteMapper::toDto));
    }

    /** Lets a CUSTOMER-role user list their own organization's sites, resolved server-side. */
    @PreAuthorize("hasRole('CUSTOMER')")
    public PageResponse<SiteDto> listMySites(Pageable pageable) {
        Long customerId = customerService.getMine().id();
        return PageResponse.from(siteRepository.findByCustomer_Id(customerId, pageable).map(siteMapper::toDto));
    }

    @PreAuthorize("hasAnyRole('DISPATCHER','MANAGER')")
    @Transactional
    public SiteDto create(Long customerId, SiteCreateRequest request) {
        Customer customer = customerService.findEntity(customerId);
        Site site = Site.builder()
                .customer(customer)
                .name(request.name())
                .addressLine(request.addressLine())
                .city(request.city())
                .state(request.state())
                .postalCode(request.postalCode())
                .active(true)
                .build();
        return siteMapper.toDto(siteRepository.save(site));
    }

    Site findEntity(Long id) {
        return siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Site " + id + " not found"));
    }
}
