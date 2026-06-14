package com.ceiba.bicycles.dto.response;

import com.ceiba.bicycles.model.Bicycle;
import com.ceiba.bicycles.model.BicycleStatus;
import com.ceiba.bicycles.model.BicycleType;

public record BicycleResponse(
        Long id,
        String code,
        BicycleType type,
        BicycleStatus status
) {
    public static BicycleResponse from(Bicycle bicycle) {
        return new BicycleResponse(
                bicycle.getId(),
                bicycle.getCode(),
                bicycle.getType(),
                bicycle.getStatus()
        );
    }
}