package com.aurionpro.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aurionpro.app.entity.Employee;
import com.aurionpro.app.entity.enums.EmployeeStatus;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    List<Employee> findAllByOrganizationId(Long organizationId);
    
    Optional<Employee> findByUserEmail(String email);
    
    List<Employee> findAllByOrganizationIdAndStatus(Long organizationId, EmployeeStatus status);
    
    @Query("SELECT e FROM Employee e WHERE e.organization.id = :organizationId "
			+ "AND e.status = com.aurionpro.app.entity.enums.EmployeeStatus.ACTIVE " + "AND e.bankAccount IS NOT NULL "
			+ "AND e.salaryStructure IS NOT NULL")
	Page<Employee> findEligibleForPayroll(Long organizationId, Pageable pageable);
    
    @Query("SELECT e FROM Employee e WHERE e.organization.id = :organizationId "
            + "AND e.status = com.aurionpro.app.entity.enums.EmployeeStatus.ACTIVE "
            + "AND e.bankAccount IS NOT NULL "
            + "AND e.salaryStructure IS NOT NULL "
            + "AND e.id NOT IN :excludedIds")
    Page<Employee> findEligibleForPayrollExcludingIds(@Param("organizationId") Long organizationId, @Param("excludedIds") List<Long> excludedIds, Pageable pageable);
}