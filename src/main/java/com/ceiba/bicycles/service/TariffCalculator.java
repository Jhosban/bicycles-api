package com.ceiba.bicycles.service;

import com.ceiba.bicycles.model.BicycleType;

public class TariffCalculator {

    public long calculateTariff(BicycleType bicycleType, long realMinutes) {
        long hours = (realMinutes + 59L) / 60L;
        return bicycleType.getValue() * hours;
    }
}
