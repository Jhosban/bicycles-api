package com.ceiba.bicycles.service;

import com.ceiba.bicycles.dto.BicycleDto;
import com.ceiba.bicycles.exception.BicycleNotFoundException;
import com.ceiba.bicycles.model.Bicycle;
import com.ceiba.bicycles.model.BicycleStatus;
import com.ceiba.bicycles.model.BicycleType;
import com.ceiba.bicycles.repository.BicycleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BicycleServiceTest {

    @Mock
    private BicycleRepository bicycleRepository;

    @InjectMocks
    private BicycleService bicycleService;

    @Test
    void shouldCreateBicycleWithDefaultStatusWhenStatusNotProvided() {
        BicycleDto request = new BicycleDto(null, "BIC-099", BicycleType.URBANA, null);
        Bicycle saved = Bicycle.builder()
                .id(1L)
                .code("BIC-099")
                .type(BicycleType.URBANA)
                .status(BicycleStatus.DISPONIBLE)
                .build();
        when(bicycleRepository.existsByCode("BIC-099")).thenReturn(false);
        when(bicycleRepository.save(any(Bicycle.class))).thenReturn(saved);

        BicycleDto response = bicycleService.create(request);

        assertThat(response.code()).isEqualTo("BIC-099");
        assertThat(response.type()).isEqualTo(BicycleType.URBANA);
        assertThat(response.status()).isEqualTo(BicycleStatus.DISPONIBLE);
        verify(bicycleRepository, times(1)).save(any(Bicycle.class));
    }

    @Test
    void shouldCreateBicycleWithProvidedStatus() {
        BicycleDto request = new BicycleDto(null, "BIC-100", BicycleType.MONTAÑA, BicycleStatus.EN_MANTENIMIENTO);
        Bicycle saved = Bicycle.builder()
                .id(2L)
                .code("BIC-100")
                .type(BicycleType.MONTAÑA)
                .status(BicycleStatus.EN_MANTENIMIENTO)
                .build();
        when(bicycleRepository.existsByCode("BIC-100")).thenReturn(false);
        when(bicycleRepository.save(any(Bicycle.class))).thenReturn(saved);

        BicycleDto response = bicycleService.create(request);

        assertThat(response.status()).isEqualTo(BicycleStatus.EN_MANTENIMIENTO);
    }

    @Test
    void shouldThrowWhenCodeAlreadyExists() {
        BicycleDto request = new BicycleDto(null, "BIC-001", BicycleType.URBANA, null);
        when(bicycleRepository.existsByCode("BIC-001")).thenReturn(true);

        assertThatThrownBy(() -> bicycleService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("BIC-001");

        verify(bicycleRepository, never()).save(any(Bicycle.class));
    }

    @Test
    void shouldReturnAvailableBicyclesWithoutTypeFilter() {
        List<Bicycle> bikes = List.of(
                Bicycle.builder().id(1L).code("BIC-001").type(BicycleType.URBANA).status(BicycleStatus.DISPONIBLE).build(),
                Bicycle.builder().id(2L).code("BIC-002").type(BicycleType.MONTAÑA).status(BicycleStatus.DISPONIBLE).build()
        );
        when(bicycleRepository.findByStatus(BicycleStatus.DISPONIBLE)).thenReturn(bikes);

        List<BicycleDto> result = bicycleService.findAvailable(null);

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldReturnAvailableBicyclesFilteredByType() {
        List<Bicycle> bikes = List.of(
                Bicycle.builder().id(1L).code("BIC-001").type(BicycleType.URBANA).status(BicycleStatus.DISPONIBLE).build()
        );
        when(bicycleRepository.findByStatusAndType(BicycleStatus.DISPONIBLE, BicycleType.URBANA))
                .thenReturn(bikes);

        List<BicycleDto> result = bicycleService.findAvailable(BicycleType.URBANA);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(BicycleType.URBANA);
    }

    @Test
    void shouldFindBicycleByCode() {
        Bicycle bike = Bicycle.builder()
                .id(1L).code("BIC-001").type(BicycleType.URBANA).status(BicycleStatus.DISPONIBLE).build();
        when(bicycleRepository.findByCode("BIC-001")).thenReturn(Optional.of(bike));

        Bicycle result = bicycleService.findByCode("BIC-001");

        assertThat(result.getCode()).isEqualTo("BIC-001");
    }

    @Test
    void shouldThrowBicycleNotFoundWhenCodeDoesNotExist() {
        when(bicycleRepository.findByCode("BIC-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bicycleService.findByCode("BIC-999"))
                .isInstanceOf(BicycleNotFoundException.class)
                .hasMessageContaining("BIC-999");
    }
}
