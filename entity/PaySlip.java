package com.aurionpro.app.entity;

import com.aurionpro.app.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.YearMonth;

@Getter
@Setter
@Entity
@Table(name = "pay_slips")
public class PaySlip extends BaseEntity {

    @Column(nullable = false)
    private YearMonth payPeriod;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal basicSalary;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal hra;

    @Column(precision = 19, scale = 2)
    private BigDecimal dearnessAllowance;

    @Column(precision = 19, scale = 2)
    private BigDecimal providentFund;

    @Column(precision = 19, scale = 2)
    private BigDecimal otherAllowances;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal netSalary;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}