package com.aurionpro.app.entity;

import com.aurionpro.app.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "bank_account_details")
public class BankAccountDetails extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false)
    private String ifscCode;

    @Column(nullable = false)
    private String bankName;

    @Column(precision = 19, scale = 2)
    private BigDecimal balance;
}