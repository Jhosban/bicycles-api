package com.ceiba.bicycles.service;

import com.ceiba.bicycles.dto.request.CreateBicycleRequest;
import com.ceiba.bicycles.dto.response.BicycleResponse;
import com.ceiba.bicycles.exception.BicycleNotFoundException;
import com.ceiba.bicycles.model.Bicycle;
import com.ceiba.bicycles.model.BicycleStatus;
import com.ceiba.bicycles.model.BicycleType;
import com.ceiba.bicycles.repository.BicycleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BicycleService {

    private final BicycleRepository bicycleRepository;

    @Transactional
    public BicycleResponse create(CreateBicycleRequest request) {
        if (bicycleRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException(
                "Bicycle with code " + request.code() + " already exists"
            );
        }

        Bicycle bicycle = Bicycle.builder()
                .code(request.code())
                .type(request.type())
                .status(request.status() != null ? request.status() : BicycleStatus.DISPONIBLE)
                .build();

        Bicycle saved = bicycleRepository.save(bicycle);
        return BicycleResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<BicycleResponse> findAvailable(BicycleType type) {
        List<Bicycle> bicycles = (type == null)
                ? bicycleRepository.findByStatus(BicycleStatus.DISPONIBLE)
                : bicycleRepository.findByStatusAndType(BicycleStatus.DISPONIBLE, type);

        return bicycles.stream()
                .map(BicycleResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Bicycle findByCode(String code) {
        return bicycleRepository.findByCode(code)
                .orElseThrow(() -> new BicycleNotFoundException(code));
    }
}
