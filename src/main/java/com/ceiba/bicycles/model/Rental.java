package com.ceiba.bicycles.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "rentals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bicycle_id", nullable = false)
    private Bicycle bicycle;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "estimated_duration_hours", nullable = false)
    private int estimatedDurationHours;

    @Column(name = "base_cost")
    private Long baseCost;

    @Column(name = "late_fee")
    private Long lateFee;

    @Column(name = "total_cost")
    private Long totalCost;

    @Column(name = "finished", nullable = false)
    private boolean finished;
}