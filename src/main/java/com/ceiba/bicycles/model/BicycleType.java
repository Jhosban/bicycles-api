package com.ceiba.bicycles.model;

public enum BicycleType {
    URBANA( 3500L),
    MONTAÑA(5000L),
    ELECTRICA(7500L);
    
    private final long value;

    BicycleType(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}
