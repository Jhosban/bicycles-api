package com.ceiba.bicycles.dto.request;

import java.time.LocalDateTime;

public record FinishRentalRequest(
        LocalDateTime endTime
) {}