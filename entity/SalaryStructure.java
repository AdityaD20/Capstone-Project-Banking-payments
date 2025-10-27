package com.aurionpro.app.entity;

import com.aurionpro.app.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "salary_structures")
public class SalaryStructure extends BaseEntity {

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal basicSalary;

    @Column(precision = 19, scale = 2)
    private BigDecimal hra;

    @Column(precision = 19, scale = 2)
    private BigDecimal da;

    @Column(precision = 19, scale = 2)
    private BigDecimal pf;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal otherAllowances;
}