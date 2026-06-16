package com.ceiba.bicycles.exception;

public class BicycleNotFoundException extends RuntimeException {
    public BicycleNotFoundException(String code) {
        super("Bicycle not found with code: " + code);
    }
}
