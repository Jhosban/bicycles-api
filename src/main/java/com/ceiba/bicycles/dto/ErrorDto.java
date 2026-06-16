package com.ceiba.bicycles.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorDto(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
    public static ErrorDto of(int status, String error, String message, String path) {
        return new ErrorDto(LocalDateTime.now(), status, error, message, path, null);
    }

    public static ErrorDto of(int status, String error, String message, String path, List<String> details) {
        return new ErrorDto(LocalDateTime.now(), status, error, message, path, details);
    }
}
