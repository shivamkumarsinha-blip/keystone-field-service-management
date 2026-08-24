package com.zidio.keystone.controller;

import com.zidio.keystone.dto.PageResponse;
import com.zidio.keystone.dto.PartCreateRequest;
import com.zidio.keystone.dto.PartDto;
import com.zidio.keystone.service.PartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parts")
@Tag(name = "Parts")
public class PartController {

    private final PartService partService;

    public PartController(PartService partService) {
        this.partService = partService;
    }

    @GetMapping
    public PageResponse<PartDto> list(@RequestParam(required = false) String search, Pageable pageable) {
        return partService.list(search, pageable);
    }

    @PostMapping
    public ResponseEntity<PartDto> create(@Valid @RequestBody PartCreateRequest request) {
        return ResponseEntity.status(201).body(partService.create(request));
    }

    @PutMapping("/{id}")
    public PartDto update(@PathVariable Long id, @Valid @RequestBody PartCreateRequest request) {
        return partService.update(id, request);
    }
}
