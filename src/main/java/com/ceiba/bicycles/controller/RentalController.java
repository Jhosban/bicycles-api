package com.ceiba.bicycles.controller;

import com.ceiba.bicycles.dto.FinishRentalDto;
import com.ceiba.bicycles.dto.RentalDto;
import com.ceiba.bicycles.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @PostMapping("/api/rentals")
    public ResponseEntity<RentalDto> startRental(
            @Valid @RequestBody RentalDto request) {
        RentalDto response = rentalService.startRental(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/api/rentals/{id}/finish")
    public ResponseEntity<RentalDto> finishRental(
            @PathVariable Long id,
            @RequestBody(required = false) FinishRentalDto request) {
        FinishRentalDto body = request != null ? request : new FinishRentalDto(null);
        return ResponseEntity.ok(rentalService.finishRental(id, body));
    }

    @GetMapping("/api/bicycles/{code}/history")
    public ResponseEntity<List<RentalDto>> findHistory(@PathVariable String code) {
        return ResponseEntity.ok(rentalService.findHistory(code));
    }
}
