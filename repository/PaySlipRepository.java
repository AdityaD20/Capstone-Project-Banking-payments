package com.aurionpro.app.repository;

import java.time.YearMonth;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aurionpro.app.entity.PaySlip;

@Repository
public interface PaySlipRepository extends JpaRepository<PaySlip, Long> {
	Page<PaySlip> findByEmployeeId(Long employeeId, Pageable pageable);
    
	@Query("SELECT p.employee.id FROM PaySlip p WHERE p.payPeriod = :payPeriod AND p.employee.organization.id = :organizationId")
	List<Long> findEmployeeIdsPaidForPeriod(@Param("payPeriod") YearMonth payPeriod, @Param("organizationId") Long organizationId);
}