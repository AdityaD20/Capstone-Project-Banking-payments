package com.aurionpro.app.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class EmployeeCreateRequestDto {
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate dateOfBirth;
}