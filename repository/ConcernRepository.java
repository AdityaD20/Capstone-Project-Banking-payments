package com.aurionpro.app.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aurionpro.app.entity.Concern;
import com.aurionpro.app.entity.enums.ConcernStatus;

@Repository
public interface ConcernRepository extends JpaRepository<Concern, Long> {

	List<Concern> findAllByEmployeeId(Long employeeId);

	Page<Concern> findByEmployeeId(Long employeeId, Pageable pageable);

	Page<Concern> findByEmployeeOrganizationId(Long organizationId, Pageable pageable);

	Page<Concern> findByEmployeeOrganizationIdAndStatus(Long organizationId, ConcernStatus status, Pageable pageable);
}