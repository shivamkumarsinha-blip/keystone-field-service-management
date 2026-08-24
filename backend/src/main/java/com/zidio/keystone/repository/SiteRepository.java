package com.zidio.keystone.repository;

import com.zidio.keystone.entity.Site;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteRepository extends JpaRepository<Site, Long> {
    Page<Site> findByCustomer_Id(Long customerId, Pageable pageable);
}
