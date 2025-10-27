package com.aurionpro.app.repository;

import com.aurionpro.app.entity.DepositRequest;
import com.aurionpro.app.entity.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepositRequestRepository extends JpaRepository<DepositRequest, Long> {
	List<DepositRequest> findByStatus(RequestStatus status);

	List<DepositRequest> findAllByOrganizationIdOrderByCreatedAtDesc(Long organizationId);
}