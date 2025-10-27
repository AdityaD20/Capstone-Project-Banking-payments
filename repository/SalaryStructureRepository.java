package com.aurionpro.app.repository;

import com.aurionpro.app.entity.SalaryStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalaryStructureRepository extends JpaRepository<SalaryStructure, Long> {

}