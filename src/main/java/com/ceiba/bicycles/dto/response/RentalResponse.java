package com.ceiba.bicycles.dto.response;

import com.ceiba.bicycles.model.Rental;

import java.time.LocalDateTime;

public record RentalResponse(
        Long id,
        String bicycleCode,
        String customerName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer estimatedDurationHours,
        Long realDurationMinutes,
        Long baseCost,
        Long lateFee,
        Long totalCost,
        Boolean hadPenalty,
        Boolean finished
) {
    public static RentalResponse from(Rental rental) {
        boolean finished = rental.isFinished();
        boolean hasPenalty = finished && rental.getLateFee() != null && rental.getLateFee() > 0;

        return new RentalResponse(
                rental.getId(),
                rental.getBicycle().getCode(),
                rental.getCustomerName(),
                rental.getStartTime(),
                rental.getEndTime(),
                rental.getEstimatedDurationHours(),
                null,
                rental.getBaseCost(),
                rental.getLateFee(),
                rental.getTotalCost(),
                hasPenalty,
                finished
        );
    }
}