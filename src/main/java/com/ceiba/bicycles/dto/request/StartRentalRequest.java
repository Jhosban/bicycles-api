package com.ceiba.bicycles.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StartRentalRequest(

        @NotBlank(message = "Bicycle code is required")
        String bicycleCode,

        @NotBlank(message = "Customer name is required")
        @Size(max = 100, message = "Customer name must not exceed 100 characters")
        String customerName,

        @NotNull(message = "Estimated duration is required")
        @Min(value = 1, message = "Estimated duration must be at least 1 hour")
        Integer estimatedDurationHours
) {}