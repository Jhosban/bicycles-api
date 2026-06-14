package com.ceiba.bicycles.service;

import com.ceiba.bicycles.model.BicycleType;

public class PenaltyCalculator {

    public long calculatePenalty(BicycleType bicycleType, long realMinutes, long estimatedMinutes) {
        long delay = realMinutes - estimatedMinutes;

        if (delay <= 0L) {
            return 0L;
        }

        long hoursDelay = (delay + 59L) / 60L;
        return (hoursDelay * bicycleType.getValue()) / 2L;
    }
}
