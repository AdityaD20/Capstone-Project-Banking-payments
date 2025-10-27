package com.aurionpro.app.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.aurionpro.app.entity.ConcernResponse;

@Repository
public interface ConcernResponseRepository extends JpaRepository<ConcernResponse, Long> {
    
    List<ConcernResponse> findAllByConcernIdOrderByCreatedAtAsc(Long concernId);
}