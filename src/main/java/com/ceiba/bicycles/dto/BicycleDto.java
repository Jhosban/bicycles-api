package com.ceiba.bicycles.dto;

import com.ceiba.bicycles.model.Bicycle;
import com.ceiba.bicycles.model.BicycleStatus;
import com.ceiba.bicycles.model.BicycleType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BicycleDto(

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Long id,

        @NotBlank(message = "Code is required")
        @Size(max = 20, message = "Code must not exceed 20 characters")
        String code,

        @NotNull(message = "Type is required")
        BicycleType type,

        BicycleStatus status
) {
    public static BicycleDto from(Bicycle bicycle) {
        return new BicycleDto(
                bicycle.getId(),
                bicycle.getCode(),
                bicycle.getType(),
                bicycle.getStatus()
        );
    }
}
