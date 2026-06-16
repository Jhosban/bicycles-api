package com.ceiba.bicycles.exception;

public class RentalNotFoundException extends RuntimeException {
    public RentalNotFoundException(Long id) {
        super("Rental not found with id: " + id);
    }

    public RentalNotFoundException(String message) {
        super(message);
    }
}
