package com.zidio.keystone.mapper;

import com.zidio.keystone.dto.PartDto;
import com.zidio.keystone.entity.Part;
import org.springframework.stereotype.Component;

@Component
public class PartMapper {
    public PartDto toDto(Part p) {
        if (p == null) return null;
        return new PartDto(p.getId(), p.getName(), p.getSku(), p.getQuantityInStock(), p.getUnitCost(), p.isActive());
    }
}
