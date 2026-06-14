package com.ceiba.bicycles.service;

import com.ceiba.bicycles.dto.request.FinishRentalRequest;
import com.ceiba.bicycles.dto.request.StartRentalRequest;
import com.ceiba.bicycles.dto.response.RentalResponse;
import com.ceiba.bicycles.exception.BicycleNotAvailableException;
import com.ceiba.bicycles.exception.BicycleNotFoundException;
import com.ceiba.bicycles.exception.RentalNotFoundException;
import com.ceiba.bicycles.model.Bicycle;
import com.ceiba.bicycles.model.BicycleStatus;
import com.ceiba.bicycles.model.BicycleType;
import com.ceiba.bicycles.model.Rental;
import com.ceiba.bicycles.repository.BicycleRepository;
import com.ceiba.bicycles.repository.RentalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private BicycleRepository bicycleRepository;

    @InjectMocks
    private RentalService rentalService;

    private Bicycle buildBicycle(String code, BicycleType type, BicycleStatus status) {
        return Bicycle.builder()
                .id(1L)
                .code(code)
                .type(type)
                .status(status)
                .build();
    }

    private Rental buildRental(Long id, Bicycle bicycle, int estimatedHours, boolean finished) {
        return Rental.builder()
                .id(id)
                .bicycle(bicycle)
                .customerName("Juan Perez")
                .startTime(LocalDateTime.of(2026, 4, 29, 10, 0))
                .estimatedDurationHours(estimatedHours)
                .finished(finished)
                .build();
    }

    // ---------- startRental tests ----------

    @Test
    void shouldStartRentalAndMarkBicycleAsAlquilada() {
        Bicycle bicycle = buildBicycle("BIC-001", BicycleType.URBANA, BicycleStatus.DISPONIBLE);
        StartRentalRequest request = new StartRentalRequest("BIC-001", "Juan Perez", 2);

        when(bicycleRepository.findByCode("BIC-001")).thenReturn(Optional.of(bicycle));
        when(rentalRepository.findFirstByBicycleCodeAndFinishedFalse("BIC-001"))
                .thenReturn(Optional.empty());
        when(rentalRepository.save(any(Rental.class))).thenAnswer(inv -> {
            Rental r = inv.getArgument(0);
            r.setId(10L);
            return r;
        });

        RentalResponse response = rentalService.startRental(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.bicycleCode()).isEqualTo("BIC-001");
        assertThat(response.customerName()).isEqualTo("Juan Perez");
        assertThat(response.finished()).isFalse();

        ArgumentCaptor<Bicycle> bicycleCaptor = ArgumentCaptor.forClass(Bicycle.class);
        verify(bicycleRepository).save(bicycleCaptor.capture());
        assertThat(bicycleCaptor.getValue().getStatus()).isEqualTo(BicycleStatus.ALQUILADA);
    }

    @Test
    void shouldThrowBicycleNotFoundWhenStartingRentalWithUnknownCode() {
        StartRentalRequest request = new StartRentalRequest("BIC-999", "Juan", 2);
        when(bicycleRepository.findByCode("BIC-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rentalService.startRental(request))
                .isInstanceOf(BicycleNotFoundException.class)
                .hasMessageContaining("BIC-999");

        verify(rentalRepository, never()).save(any(Rental.class));
    }

    @Test
    void shouldThrowWhenBicycleHasActiveRental() {
        Bicycle bicycle = buildBicycle("BIC-001", BicycleType.URBANA, BicycleStatus.DISPONIBLE);
        Rental active = buildRental(5L, bicycle, 2, false);
        StartRentalRequest request = new StartRentalRequest("BIC-001", "Juan", 2);

        when(bicycleRepository.findByCode("BIC-001")).thenReturn(Optional.of(bicycle));
        when(rentalRepository.findFirstByBicycleCodeAndFinishedFalse("BIC-001"))
                .thenReturn(Optional.of(active));

        assertThatThrownBy(() -> rentalService.startRental(request))
                .isInstanceOf(BicycleNotAvailableException.class)
                .hasMessageContaining("active rental");

        verify(rentalRepository, never()).save(any(Rental.class));
    }

    @Test
    void shouldThrowWhenBicycleStatusIsNotDisponible() {
        Bicycle bicycle = buildBicycle("BIC-004", BicycleType.MONTAÑA, BicycleStatus.EN_MANTENIMIENTO);
        StartRentalRequest request = new StartRentalRequest("BIC-004", "Juan", 2);

        when(bicycleRepository.findByCode("BIC-004")).thenReturn(Optional.of(bicycle));
        when(rentalRepository.findFirstByBicycleCodeAndFinishedFalse("BIC-004"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> rentalService.startRental(request))
                .isInstanceOf(BicycleNotAvailableException.class)
                .hasMessageContaining("EN_MANTENIMIENTO");

        verify(rentalRepository, never()).save(any(Rental.class));
    }

    // ---------- finishRental tests ----------

    @Test
    void shouldFinishRentalWithNoPenaltyWhenOnTime() {
        Bicycle bicycle = buildBicycle("BIC-001", BicycleType.URBANA, BicycleStatus.ALQUILADA);
        Rental rental = buildRental(10L, bicycle, 2, false);
        LocalDateTime endTime = LocalDateTime.of(2026, 4, 29, 12, 0);

        when(rentalRepository.findById(10L)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental);

        RentalResponse response = rentalService.finishRental(10L, new FinishRentalRequest(endTime));

        assertThat(response.finished()).isTrue();
        assertThat(response.baseCost()).isEqualTo(7_000L);
        assertThat(response.lateFee()).isEqualTo(0L);
        assertThat(response.totalCost()).isEqualTo(7_000L);
        assertThat(response.hadPenalty()).isFalse();
    }

    @Test
    void shouldFinishRentalWithPenaltyForLateReturn() {
        Bicycle bicycle = buildBicycle("BIC-002", BicycleType.MONTAÑA, BicycleStatus.ALQUILADA);
        Rental rental = buildRental(10L, bicycle, 2, false);
        LocalDateTime endTime = LocalDateTime.of(2026, 4, 29, 13, 20);

        when(rentalRepository.findById(10L)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental);

        RentalResponse response = rentalService.finishRental(10L, new FinishRentalRequest(endTime));

        assertThat(response.finished()).isTrue();
        assertThat(response.baseCost()).isEqualTo(20_000L);
        assertThat(response.lateFee()).isEqualTo(5_000L);
        assertThat(response.totalCost()).isEqualTo(25_000L);
        assertThat(response.hadPenalty()).isTrue();
    }

    @Test
    void shouldUseCurrentTimeWhenEndTimeIsNull() {
        Bicycle bicycle = buildBicycle("BIC-001", BicycleType.URBANA, BicycleStatus.ALQUILADA);
        Rental rental = buildRental(10L, bicycle, 1, false);

        when(rentalRepository.findById(10L)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental);

        RentalResponse response = rentalService.finishRental(10L, new FinishRentalRequest(null));

        assertThat(response.finished()).isTrue();
        assertThat(response.endTime()).isNotNull();
    }

    @Test
    void shouldThrowWhenRentalNotFound() {
        when(rentalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rentalService.finishRental(99L, new FinishRentalRequest(null)))
                .isInstanceOf(RentalNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void shouldThrowWhenRentalAlreadyFinished() {
        Bicycle bicycle = buildBicycle("BIC-001", BicycleType.URBANA, BicycleStatus.DISPONIBLE);
        Rental rental = buildRental(10L, bicycle, 2, true);

        when(rentalRepository.findById(10L)).thenReturn(Optional.of(rental));

        assertThatThrownBy(() -> rentalService.finishRental(10L, new FinishRentalRequest(null)))
                .isInstanceOf(RentalNotFoundException.class)
                .hasMessageContaining("already been finished");
    }

    @Test
    void shouldMarkBicycleAsDisponibleWhenRentalFinishes() {
        Bicycle bicycle = buildBicycle("BIC-001", BicycleType.URBANA, BicycleStatus.ALQUILADA);
        Rental rental = buildRental(10L, bicycle, 1, false);

        when(rentalRepository.findById(10L)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental);

        rentalService.finishRental(10L, new FinishRentalRequest(null));

        ArgumentCaptor<Bicycle> captor = ArgumentCaptor.forClass(Bicycle.class);
        verify(bicycleRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(BicycleStatus.DISPONIBLE);
    }

    // ---------- findHistory tests ----------

    @Test
    void shouldReturnHistoryOrderedByStartTimeDesc() {
        Bicycle bicycle = buildBicycle("BIC-001", BicycleType.URBANA, BicycleStatus.DISPONIBLE);
        Rental r1 = buildRental(1L, bicycle, 2, true);
        Rental r2 = buildRental(2L, bicycle, 3, false);

        when(bicycleRepository.existsByCode("BIC-001")).thenReturn(true);
        when(rentalRepository.findByBicycleCodeOrderByStartTimeDesc("BIC-001"))
                .thenReturn(List.of(r2, r1));

        List<RentalResponse> history = rentalService.findHistory("BIC-001");

        assertThat(history).hasSize(2);
        assertThat(history.get(0).id()).isEqualTo(2L);
        assertThat(history.get(1).id()).isEqualTo(1L);
    }

    @Test
    void shouldThrowWhenAskingHistoryForUnknownBicycle() {
        when(bicycleRepository.existsByCode("BIC-999")).thenReturn(false);

        assertThatThrownBy(() -> rentalService.findHistory("BIC-999"))
                .isInstanceOf(BicycleNotFoundException.class)
                .hasMessageContaining("BIC-999");
    }
}
