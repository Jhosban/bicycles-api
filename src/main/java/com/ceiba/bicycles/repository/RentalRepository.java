package com.ceiba.bicycles.repository;

import com.ceiba.bicycles.model.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {

    List<Rental> findByBicycleCodeOrderByStartTimeDesc(String code);

    Optional<Rental> findFirstByBicycleCodeAndFinishedFalse(String code);
}
