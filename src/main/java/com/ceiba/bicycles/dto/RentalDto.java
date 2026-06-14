package com.ceiba.bicycles.dto;

import com.ceiba.bicycles.model.Rental;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record RentalDto(

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Long id,

        @NotBlank(message = "Bicycle code is required")
        String bicycleCode,

        @NotBlank(message = "Customer name is required")
        @Size(max = 100, message = "Customer name must not exceed 100 characters")
        String customerName,

        @NotNull(message = "Estimated duration is required")
        @Min(value = 1, message = "Estimated duration must be at least 1 hour")
        Integer estimatedDurationHours,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Long realDurationMinutes,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        LocalDateTime startTime,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        LocalDateTime endTime,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Long baseCost,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Long lateFee,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Long totalCost,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Boolean hadPenalty,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Boolean finished
) {
    public static RentalDto from(Rental rental) {
        boolean finished = rental.isFinished();
        boolean hasPenalty = finished
                && rental.getLateFee() != null
                && rental.getLateFee() > 0;

        return new RentalDto(
                rental.getId(),
                rental.getBicycle().getCode(),
                rental.getCustomerName(),
                rental.getEstimatedDurationHours(),
                null,
                rental.getStartTime(),
                rental.getEndTime(),
                rental.getBaseCost(),
                rental.getLateFee(),
                rental.getTotalCost(),
                hasPenalty,
                finished
        );
    }
}
