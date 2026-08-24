package com.zidio.keystone.service;

import com.zidio.keystone.dto.PageResponse;
import com.zidio.keystone.dto.PartCreateRequest;
import com.zidio.keystone.dto.PartDto;
import com.zidio.keystone.entity.Part;
import com.zidio.keystone.exception.BadRequestException;
import com.zidio.keystone.exception.ResourceNotFoundException;
import com.zidio.keystone.mapper.PartMapper;
import com.zidio.keystone.repository.PartRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PartService {

    private final PartRepository partRepository;
    private final PartMapper partMapper;

    public PartService(PartRepository partRepository, PartMapper partMapper) {
        this.partRepository = partRepository;
        this.partMapper = partMapper;
    }

    public PageResponse<PartDto> list(String search, Pageable pageable) {
        var page = (search == null || search.isBlank())
                ? partRepository.findAll(pageable)
                : partRepository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(search, search, pageable);
        return PageResponse.from(page.map(partMapper::toDto));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public PartDto create(PartCreateRequest request) {
        Part part = Part.builder()
                .name(request.name())
                .sku(request.sku())
                .quantityInStock(request.quantityInStock() == null ? 0 : request.quantityInStock())
                .unitCost(request.unitCost() == null ? BigDecimal.ZERO : request.unitCost())
                .active(true)
                .build();
        return partMapper.toDto(partRepository.save(part));
    }

    @PreAuthorize("hasRole('MANAGER')")
    @Transactional
    public PartDto update(Long id, PartCreateRequest request) {
        Part part = findEntity(id);
        part.setName(request.name());
        part.setSku(request.sku());
        if (request.quantityInStock() != null) part.setQuantityInStock(request.quantityInStock());
        if (request.unitCost() != null) part.setUnitCost(request.unitCost());
        return partMapper.toDto(part);
    }

    Part findEntity(Long id) {
        return partRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Part " + id + " not found"));
    }
}
