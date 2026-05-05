package com.example.store.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrderCreateRequest {

    @NotNull(message = "customerId must not be null") private Long customerId;

    @NotBlank(message = "description must not be blank")
    private String description;

    @NotNull(message = "productIds must not be null") private List<Long> productIds = new ArrayList<>();
}
