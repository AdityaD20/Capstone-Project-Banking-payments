
package com.aurionpro.app.entity;

import com.aurionpro.app.entity.common.BaseEntity;
import com.aurionpro.app.entity.enums.ConcernStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "concerns")
public class Concern extends BaseEntity {

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConcernStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
