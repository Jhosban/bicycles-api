package com.ceiba.bicycles.controller;

import com.ceiba.bicycles.dto.BicycleDto;
import com.ceiba.bicycles.model.BicycleType;
import com.ceiba.bicycles.service.BicycleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bicycles")
@RequiredArgsConstructor
public class BicycleController {

    private final BicycleService bicycleService;

    @PostMapping
    public ResponseEntity<BicycleDto> create(@Valid @RequestBody BicycleDto request) {
        BicycleDto response = bicycleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/available")
    public ResponseEntity<List<BicycleDto>> findAvailable(
            @RequestParam(required = false) BicycleType type) {
        return ResponseEntity.ok(bicycleService.findAvailable(type));
    }
}
