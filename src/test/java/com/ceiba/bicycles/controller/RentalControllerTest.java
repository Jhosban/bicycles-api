package com.ceiba.bicycles.controller;

import com.ceiba.bicycles.dto.request.FinishRentalRequest;
import com.ceiba.bicycles.dto.request.StartRentalRequest;
import com.ceiba.bicycles.dto.response.RentalResponse;
import com.ceiba.bicycles.exception.BicycleNotAvailableException;
import com.ceiba.bicycles.exception.BicycleNotFoundException;
import com.ceiba.bicycles.exception.GlobalExceptionHandler;
import com.ceiba.bicycles.exception.RentalNotFoundException;
import com.ceiba.bicycles.service.RentalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RentalController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class RentalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private RentalService rentalService;

    private RentalResponse buildRentalResponse() {
        return new RentalResponse(
                10L, "BIC-001", "Juan Perez",
                LocalDateTime.of(2026, 4, 29, 10, 0),
                null, 2, null, null, null, null, false, false
        );
    }

    // ---------- startRental ----------

    @Test
    void shouldStartRentalAndReturn201() throws Exception {
        StartRentalRequest request = new StartRentalRequest("BIC-001", "Juan Perez", 2);
        when(rentalService.startRental(any(StartRentalRequest.class)))
                .thenReturn(buildRentalResponse());

        mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.bicycleCode").value("BIC-001"))
                .andExpect(jsonPath("$.customerName").value("Juan Perez"))
                .andExpect(jsonPath("$.finished").value(false));
    }

    @Test
    void shouldReturn400WhenBicycleCodeIsBlank() throws Exception {
        StartRentalRequest request = new StartRentalRequest("", "Juan", 2);

        mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void shouldReturn409WhenBicycleNotAvailable() throws Exception {
        StartRentalRequest request = new StartRentalRequest("BIC-001", "Juan", 2);
        when(rentalService.startRental(any(StartRentalRequest.class)))
                .thenThrow(new BicycleNotAvailableException("Bicycle BIC-001 is not available"));

        mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void shouldReturn404WhenBicycleNotFound() throws Exception {
        StartRentalRequest request = new StartRentalRequest("BIC-999", "Juan", 2);
        when(rentalService.startRental(any(StartRentalRequest.class)))
                .thenThrow(new BicycleNotFoundException("BIC-999"));

        mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ---------- finishRental ----------

    @Test
    void shouldFinishRentalAndReturn200() throws Exception {
        RentalResponse finished = new RentalResponse(
                10L, "BIC-001", "Juan Perez",
                LocalDateTime.of(2026, 4, 29, 10, 0),
                LocalDateTime.of(2026, 4, 29, 12, 0),
                2, 120L, 7_000L, 0L, 7_000L, false, true
        );
        when(rentalService.finishRental(eq(10L), any(FinishRentalRequest.class)))
                .thenReturn(finished);

        FinishRentalRequest body = new FinishRentalRequest(
                LocalDateTime.of(2026, 4, 29, 12, 0)
        );
        mockMvc.perform(post("/api/rentals/10/finish")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finished").value(true))
                .andExpect(jsonPath("$.baseCost").value(7_000))
                .andExpect(jsonPath("$.totalCost").value(7_000))
                .andExpect(jsonPath("$.hadPenalty").value(false));
    }

    @Test
    void shouldFinishRentalWhenBodyIsNull() throws Exception {
        RentalResponse finished = new RentalResponse(
                10L, "BIC-001", "Juan Perez",
                LocalDateTime.of(2026, 4, 29, 10, 0),
                LocalDateTime.of(2026, 4, 29, 12, 0),
                2, 120L, 7_000L, 0L, 7_000L, false, true
        );
        when(rentalService.finishRental(eq(10L), any(FinishRentalRequest.class)))
                .thenReturn(finished);

        mockMvc.perform(post("/api/rentals/10/finish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finished").value(true));
    }

    @Test
    void shouldReturn404WhenRentalNotFound() throws Exception {
        when(rentalService.finishRental(eq(99L), any(FinishRentalRequest.class)))
                .thenThrow(new RentalNotFoundException(99L));

        mockMvc.perform(post("/api/rentals/99/finish"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ---------- findHistory ----------

    @Test
    void shouldReturnHistoryForBicycle() throws Exception {
        List<RentalResponse> history = List.of(
                new RentalResponse(2L, "BIC-001", "Maria",
                        LocalDateTime.of(2026, 4, 29, 10, 0), null,
                        3, null, null, null, null, false, false),
                new RentalResponse(1L, "BIC-001", "Juan",
                        LocalDateTime.of(2026, 4, 28, 10, 0),
                        LocalDateTime.of(2026, 4, 28, 12, 0),
                        2, 120L, 7_000L, 0L, 7_000L, false, true)
        );
        when(rentalService.findHistory("BIC-001")).thenReturn(history);

        mockMvc.perform(get("/api/bicycles/BIC-001/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].customerName").value("Maria"));
    }

    @Test
    void shouldReturn404WhenBicycleNotFoundForHistory() throws Exception {
        when(rentalService.findHistory("BIC-999"))
                .thenThrow(new BicycleNotFoundException("BIC-999"));

        mockMvc.perform(get("/api/bicycles/BIC-999/history"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
