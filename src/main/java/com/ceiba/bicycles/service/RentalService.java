package com.ceiba.bicycles.service;

import com.ceiba.bicycles.dto.FinishRentalDto;
import com.ceiba.bicycles.dto.RentalDto;
import com.ceiba.bicycles.exception.BicycleNotAvailableException;
import com.ceiba.bicycles.exception.BicycleNotFoundException;
import com.ceiba.bicycles.exception.RentalNotFoundException;
import com.ceiba.bicycles.model.Bicycle;
import com.ceiba.bicycles.model.BicycleStatus;
import com.ceiba.bicycles.model.Rental;
import com.ceiba.bicycles.repository.BicycleRepository;
import com.ceiba.bicycles.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final BicycleRepository bicycleRepository;
    private final TariffCalculator tariffCalculator = new TariffCalculator();
    private final PenaltyCalculator penaltyCalculator = new PenaltyCalculator();

    @Transactional
    public RentalDto startRental(RentalDto request) {
        Bicycle bicycle = bicycleRepository.findByCode(request.bicycleCode())
                .orElseThrow(() -> new BicycleNotFoundException(request.bicycleCode()));

        Optional<Rental> activeRental = rentalRepository
                .findFirstByBicycleCodeAndFinishedFalse(request.bicycleCode());
        if (activeRental.isPresent()) {
            throw new BicycleNotAvailableException(
                "Bicycle " + request.bicycleCode() + " already has an active rental"
            );
        }

        if (bicycle.getStatus() != BicycleStatus.DISPONIBLE) {
            throw new BicycleNotAvailableException(
                "Bicycle " + request.bicycleCode()
                + " is not available (current status: " + bicycle.getStatus() + ")"
            );
        }

        Rental rental = Rental.builder()
                .bicycle(bicycle)
                .customerName(request.customerName())
                .startTime(LocalDateTime.now())
                .estimatedDurationHours(request.estimatedDurationHours())
                .finished(false)
                .build();

        bicycle.setStatus(BicycleStatus.ALQUILADA);
        bicycleRepository.save(bicycle);

        Rental saved = rentalRepository.save(rental);
        return RentalDto.from(saved);
    }

    @Transactional
    public RentalDto finishRental(Long rentalId, FinishRentalDto request) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RentalNotFoundException(rentalId));

        if (rental.isFinished()) {
            throw new RentalNotFoundException(
                "Rental " + rentalId + " has already been finished"
            );
        }

        LocalDateTime endTime = request.endTime() != null
                ? request.endTime()
                : LocalDateTime.now();

        long realMinutes = ChronoUnit.MINUTES.between(rental.getStartTime(), endTime);
        long estimatedMinutes = (long) rental.getEstimatedDurationHours() * 60L;

        long baseCost = tariffCalculator.calculateTariff(
                rental.getBicycle().getType(), realMinutes
        );
        long lateFee = penaltyCalculator.calculatePenalty(
                rental.getBicycle().getType(), realMinutes, (int) estimatedMinutes
        );

        rental.setEndTime(endTime);
        rental.setBaseCost(baseCost);
        rental.setLateFee(lateFee);
        rental.setTotalCost(baseCost + lateFee);
        rental.setFinished(true);

        Bicycle bicycle = rental.getBicycle();
        bicycle.setStatus(BicycleStatus.DISPONIBLE);
        bicycleRepository.save(bicycle);

        Rental saved = rentalRepository.save(rental);
        return RentalDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<RentalDto> findHistory(String bicycleCode) {
        if (!bicycleRepository.existsByCode(bicycleCode)) {
            throw new BicycleNotFoundException(bicycleCode);
        }

        List<Rental> rentals = rentalRepository
                .findByBicycleCodeOrderByStartTimeDesc(bicycleCode);
        return rentals.stream()
                .map(RentalDto::from)
                .toList();
    }
}
