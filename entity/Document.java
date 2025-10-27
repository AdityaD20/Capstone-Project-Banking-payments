package com.aurionpro.app.entity;

import com.aurionpro.app.entity.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "documents")
public class Document extends BaseEntity {

    @Column(nullable = false)
    private String url;

    private String type;
    
    @Column
    private String displayName;
    
    @Column(nullable = false)
    private String entityName;
    
    @Column(nullable = false)
    private Long entityId;
}