package com.ceiba.bicycles.controller;

import com.ceiba.bicycles.dto.request.FinishRentalRequest;
import com.ceiba.bicycles.dto.request.StartRentalRequest;
import com.ceiba.bicycles.dto.response.RentalResponse;
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
    public ResponseEntity<RentalResponse> startRental(
            @Valid @RequestBody StartRentalRequest request) {
        RentalResponse response = rentalService.startRental(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/api/rentals/{id}/finish")
    public ResponseEntity<RentalResponse> finishRental(
            @PathVariable Long id,
            @RequestBody(required = false) FinishRentalRequest request) {
        FinishRentalRequest body = request != null ? request : new FinishRentalRequest(null);
        return ResponseEntity.ok(rentalService.finishRental(id, body));
    }

    @GetMapping("/api/bicycles/{code}/history")
    public ResponseEntity<List<RentalResponse>> findHistory(@PathVariable String code) {
        return ResponseEntity.ok(rentalService.findHistory(code));
    }
}
