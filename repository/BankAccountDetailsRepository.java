package com.aurionpro.app.repository;

import com.aurionpro.app.entity.BankAccountDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankAccountDetailsRepository extends JpaRepository<BankAccountDetails, Long> {

}