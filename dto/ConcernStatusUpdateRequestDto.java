package com.aurionpro.app.dto;

import com.aurionpro.app.entity.enums.ConcernStatus;
import lombok.Data;

@Data
public class ConcernStatusUpdateRequestDto {
    private ConcernStatus newStatus;
}