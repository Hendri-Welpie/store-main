package com.example.store.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

/** Request body for creating a new customer. Decouples the API contract from the JPA entity. */
@Data
public class CustomerCreateRequest {

    @NotBlank(message = "Customer name must not be blank")
    private String name;
}
