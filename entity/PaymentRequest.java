package com.aurionpro.app.entity;

import java.math.BigDecimal;
import java.util.List;

import com.aurionpro.app.entity.common.BaseEntity;
import com.aurionpro.app.entity.enums.PaymentType;
import com.aurionpro.app.entity.enums.RequestStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "payment_requests")
public class PaymentRequest extends BaseEntity {

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType type;

    private String rejectionReason;
    
    @Column(length = 255)
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id") 
    private Vendor vendor;
    
    @OneToMany(mappedBy = "paymentRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentRequestItem> items;
}