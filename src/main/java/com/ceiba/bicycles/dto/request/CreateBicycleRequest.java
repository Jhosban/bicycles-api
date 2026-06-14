package com.ceiba.bicycles.dto.request;

import com.ceiba.bicycles.model.BicycleStatus;
import com.ceiba.bicycles.model.BicycleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateBicycleRequest(

        @NotBlank(message = "Code is required")
        @Size(max = 20, message = "Code must not exceed 20 characters")
        String code,

        @NotNull(message = "Type is required")
        BicycleType type,

        BicycleStatus status
) {}