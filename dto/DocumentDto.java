package com.aurionpro.app.dto;

import lombok.Data;

@Data
public class DocumentDto {
    private Long id;
    private String url;
    private String type;
    private String displayName;
}