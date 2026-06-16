package com.ceiba.bicycles.controller;

import com.ceiba.bicycles.dto.FinishRentalDto;
import com.ceiba.bicycles.dto.RentalDto;
import com.ceiba.bicycles.exception.BicycleNotAvailableException;
import com.ceiba.bicycles.exception.BicycleNotFoundException;
import com.ceiba.bicycles.exception.GlobalExceptionHandler;
import com.ceiba.bicycles.exception.RentalNotFoundException;
import com.ceiba.bicycles.security.JsonAuthenticationEntryPoint;
import com.ceiba.bicycles.security.JwtService;
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

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint;

    private RentalDto buildRentalDto() {
        return new RentalDto(
                10L, "BIC-001", "Juan Perez", 2,
                null,
                LocalDateTime.of(2026, 4, 29, 10, 0),
                null, null, null, null, null, false
        );
    }

    // ---------- startRental ----------

    @Test
    void shouldStartRentalAndReturn201() throws Exception {
        RentalDto request = new RentalDto(
                null, "BIC-001", "Juan Perez", 2,
                null, null, null, null, null, null, null, null
        );
        when(rentalService.startRental(any(RentalDto.class)))
                .thenReturn(buildRentalDto());

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
        RentalDto request = new RentalDto(
                null, "", "Juan", 2,
                null, null, null, null, null, null, null, null
        );

        mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void shouldReturn409WhenBicycleNotAvailable() throws Exception {
        RentalDto request = new RentalDto(
                null, "BIC-001", "Juan", 2,
                null, null, null, null, null, null, null, null
        );
        when(rentalService.startRental(any(RentalDto.class)))
                .thenThrow(new BicycleNotAvailableException("Bicycle BIC-001 is not available"));

        mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void shouldReturn404WhenBicycleNotFound() throws Exception {
        RentalDto request = new RentalDto(
                null, "BIC-999", "Juan", 2,
                null, null, null, null, null, null, null, null
        );
        when(rentalService.startRental(any(RentalDto.class)))
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
        RentalDto finished = new RentalDto(
                10L, "BIC-001", "Juan Perez", 2,
                120L,
                LocalDateTime.of(2026, 4, 29, 10, 0),
                LocalDateTime.of(2026, 4, 29, 12, 0),
                7_000L, 0L, 7_000L, false, true
        );
        when(rentalService.finishRental(eq(10L), any(FinishRentalDto.class)))
                .thenReturn(finished);

        FinishRentalDto body = new FinishRentalDto(LocalDateTime.of(2026, 4, 29, 12, 0));
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
        RentalDto finished = new RentalDto(
                10L, "BIC-001", "Juan Perez", 2,
                120L,
                LocalDateTime.of(2026, 4, 29, 10, 0),
                LocalDateTime.of(2026, 4, 29, 12, 0),
                7_000L, 0L, 7_000L, false, true
        );
        when(rentalService.finishRental(eq(10L), any(FinishRentalDto.class)))
                .thenReturn(finished);

        mockMvc.perform(post("/api/rentals/10/finish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finished").value(true));
    }

    @Test
    void shouldReturn404WhenRentalNotFound() throws Exception {
        when(rentalService.finishRental(eq(99L), any(FinishRentalDto.class)))
                .thenThrow(new RentalNotFoundException(99L));

        mockMvc.perform(post("/api/rentals/99/finish"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ---------- findHistory ----------

    @Test
    void shouldReturnHistoryForBicycle() throws Exception {
        List<RentalDto> history = List.of(
                new RentalDto(2L, "BIC-001", "Maria", 3,
                        null,
                        LocalDateTime.of(2026, 4, 29, 10, 0),
                        null, null, null, null, null, false),
                new RentalDto(1L, "BIC-001", "Juan", 2,
                        120L,
                        LocalDateTime.of(2026, 4, 28, 10, 0),
                        LocalDateTime.of(2026, 4, 28, 12, 0),
                        7_000L, 0L, 7_000L, false, true)
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
