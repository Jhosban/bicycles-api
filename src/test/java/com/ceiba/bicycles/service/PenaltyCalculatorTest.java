package com.ceiba.bicycles.service;

import com.ceiba.bicycles.model.BicycleType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PenaltyCalculatorTest {

    private final PenaltyCalculator penaltyCalculator = new PenaltyCalculator();

    @Test
    void shouldReturnZeroWhenThereIsNoDelay() {
        long result = penaltyCalculator.calculatePenalty(BicycleType.URBANA, 120L, 120);

        assertEquals(0L, result);
    }

    @Test
    void shouldReturnZeroWhenBicycleIsReturnedEarly() {
        long result = penaltyCalculator.calculatePenalty(BicycleType.URBANA, 90L, 120);

        assertEquals(0L, result);
    }

    @Test
    void shouldCalculatePenaltyForMountainBicycleFromStatementExample() {
        long result = penaltyCalculator.calculatePenalty(BicycleType.MONTAÑA, 200L, 120);

        assertEquals(5000L, result);
    }

    @Test
    void shouldCalculateMinimumPenaltyForThirtyMinutesDelay() {
        long result = penaltyCalculator.calculatePenalty(BicycleType.MONTAÑA, 150L, 120);

        assertEquals(2500L, result);
    }

    @Test
    void shouldReturnZeroWhenReturnIsExactlyOnLimit() {
        long result = penaltyCalculator.calculatePenalty(BicycleType.MONTAÑA, 120L, 120);

        assertEquals(0L, result);
    }

    @Test
    void shouldCalculatePenaltyForUrbanBicycleWithOneHourAndThirtyMinutesDelay() {
        long result = penaltyCalculator.calculatePenalty(BicycleType.URBANA, 210L, 120);

        assertEquals(3500L, result);
    }

    @Test
    void shouldCalculatePenaltyForElectricBicycleWithThirtyMinutesDelay() {
        long result = penaltyCalculator.calculatePenalty(BicycleType.ELECTRICA, 150L, 120);

        assertEquals(3750L, result);
    }
}