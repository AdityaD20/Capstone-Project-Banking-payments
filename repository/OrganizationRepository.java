package com.aurionpro.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aurionpro.app.entity.Organization;
import com.aurionpro.app.entity.enums.OrganizationStatus;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
	Optional<Organization> findByUserId(Long userId);

	List<Organization> findByStatus(OrganizationStatus status);

	Optional<Organization> findByUserEmail(String email);
	
    // Re-adding the explicit query for filtering by status and not deleted
	@Query("SELECT o FROM Organization o WHERE o.deleted = false AND o.status IN :statuses")
	List<Organization> findByStatusInAndDeletedFalse(@Param("statuses") List<OrganizationStatus> statuses);

    // Re-adding the explicit query for finding all non-deleted orgs
    @Query("SELECT o FROM Organization o WHERE o.deleted = false")
    List<Organization> findAllByDeletedFalse();
	
	boolean existsByNameIgnoreCase(String name);
}