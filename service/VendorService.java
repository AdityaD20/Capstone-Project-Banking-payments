// src/main/java/com/aurionpro/app/service/VendorService.java
package com.aurionpro.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.aurionpro.app.dto.VendorCreateRequestDto;
import com.aurionpro.app.dto.VendorResponseDto;

public interface VendorService {
    VendorResponseDto addVendor(VendorCreateRequestDto createDto);

	Page<VendorResponseDto> getAllVendors(Pageable pageable);
}