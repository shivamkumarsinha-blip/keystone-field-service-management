package com.zidio.keystone.mapper;

import com.zidio.keystone.dto.SiteDto;
import com.zidio.keystone.entity.Site;
import org.springframework.stereotype.Component;

@Component
public class SiteMapper {
    public SiteDto toDto(Site s) {
        if (s == null) return null;
        return new SiteDto(s.getId(), s.getCustomer().getId(), s.getName(), s.getAddressLine(),
                s.getCity(), s.getState(), s.getPostalCode(), s.isActive());
    }
}
