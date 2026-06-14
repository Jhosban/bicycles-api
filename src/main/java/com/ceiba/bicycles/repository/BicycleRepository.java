package com.ceiba.bicycles.repository;

import com.ceiba.bicycles.model.Bicycle;
import com.ceiba.bicycles.model.BicycleStatus;
import com.ceiba.bicycles.model.BicycleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BicycleRepository extends JpaRepository<Bicycle, Long> {

    Optional<Bicycle> findByCode(String code);

    boolean existsByCode(String code);

    List<Bicycle> findByStatus(BicycleStatus status);

    List<Bicycle> findByType(BicycleType type);

    List<Bicycle> findByStatusAndType(BicycleStatus status, BicycleType type);
}
