package com.aurionpro.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aurionpro.app.entity.PaymentRequestItem;

@Repository
public interface PaymentRequestItemRepository extends JpaRepository<PaymentRequestItem, Long> {
	
	 Page<PaymentRequestItem> findByPaymentRequestId(Long paymentRequestId, Pageable pageable);
}