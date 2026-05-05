package com.example.store.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

/** Request body for creating a new product. */
@Data
public class ProductCreateRequest {

    @NotBlank(message = "description must not be blank")
    private String description;
}
