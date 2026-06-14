package com.ceiba.bicycles.exception;

public class BicycleNotAvailableException extends RuntimeException {
    public BicycleNotAvailableException(String message) {
        super(message);
    }
}
