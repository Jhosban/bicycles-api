package com.ceiba.bicycles.service;

import com.ceiba.bicycles.model.BicycleType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TariffCalculatorTest {

    private final TariffCalculator tariffCalculator = new TariffCalculator();

    @Test
    void shouldCalculateTwoHoursForUrbanBicycleWhenRealMinutesAreSeventy() {
        long result = tariffCalculator.calculateTariff(BicycleType.URBANA, 70L);

        assertEquals(7000L, result);
    }

    @Test
    void shouldCalculateTwoHoursForMountainBicycleWhenRealMinutesAreSeventy() {
        long result = tariffCalculator.calculateTariff(BicycleType.MONTAÑA, 70L);

        assertEquals(10000L, result);
    }

    @Test
    void shouldCalculateTwoHoursForElectricBicycleWhenRealMinutesAreSeventy() {
        long result = tariffCalculator.calculateTariff(BicycleType.ELECTRICA, 70L);

        assertEquals(15000L, result);
    }

    @Test
    void shouldCalculateTwoHoursForUrbanBicycleWhenRealMinutesAreOneHundredTwenty() {
        long result = tariffCalculator.calculateTariff(BicycleType.URBANA, 120L);

        assertEquals(7000L, result);
    }

    @Test
    void shouldCalculateOneHourForUrbanBicycleWhenRealMinutesAreFiftyNine() {
        long result = tariffCalculator.calculateTariff(BicycleType.URBANA, 59L);

        assertEquals(3500L, result);
    }

    @Test
    void shouldCalculateOneHourForUrbanBicycleWhenRealMinutesAreSixty() {
        long result = tariffCalculator.calculateTariff(BicycleType.URBANA, 60L);

        assertEquals(3500L, result);
    }

    @Test
    void shouldCalculateOneHourForMountainBicycle() {
        long result = tariffCalculator.calculateTariff(BicycleType.MONTAÑA, 60L);

        assertEquals(5000L, result);
    }

    @Test
    void shouldCalculateOneHourForElectricBicycle() {
        long result = tariffCalculator.calculateTariff(BicycleType.ELECTRICA, 60L);

        assertEquals(7500L, result);
    }

    @Test
    void shouldReturnZeroWhenRealMinutesAreZero() {
        long result = tariffCalculator.calculateTariff(BicycleType.URBANA, 0L);

        assertEquals(0L, result);
    }


}