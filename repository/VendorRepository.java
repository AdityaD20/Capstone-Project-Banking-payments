package com.aurionpro.app.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aurionpro.app.entity.Vendor;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
	
    List<Vendor> findAllByOrganizationId(Long organizationId);
    
    Page<Vendor> findByOrganizationIdAndDeletedFalse(Long organizationId, Pageable pageable);
}