package com.aurionpro.app.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.aurionpro.app.entity.PaymentRequest;
import com.aurionpro.app.entity.enums.PaymentType;
import com.aurionpro.app.entity.enums.RequestStatus;

@Repository
public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, Long>, JpaSpecificationExecutor<PaymentRequest> {
    
    // THIS IS THE NEW METHOD
    List<PaymentRequest> findAllByOrganizationId(Long organizationId);
    
    List<PaymentRequest> findByStatus(RequestStatus status);
    
    boolean existsByOrganizationIdAndTypeAndStatusAndCreatedAtBetween(
            Long organizationId,
            PaymentType type,
            RequestStatus status,
            LocalDateTime startOfMonth,
            LocalDateTime endOfMonth
        );
}