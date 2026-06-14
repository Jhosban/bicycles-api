package com.ceiba.bicycles.controller;

import com.ceiba.bicycles.dto.request.CreateBicycleRequest;
import com.ceiba.bicycles.dto.response.BicycleResponse;
import com.ceiba.bicycles.exception.GlobalExceptionHandler;
import com.ceiba.bicycles.model.BicycleStatus;
import com.ceiba.bicycles.model.BicycleType;
import com.ceiba.bicycles.service.BicycleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BicycleController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class BicycleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private BicycleService bicycleService;

    @Test
    void shouldCreateBicycleAndReturn201() throws Exception {
        CreateBicycleRequest request = new CreateBicycleRequest("BIC-099", BicycleType.URBANA, null);
        BicycleResponse response = new BicycleResponse(
                1L, "BIC-099", BicycleType.URBANA, BicycleStatus.DISPONIBLE
        );
        when(bicycleService.create(any(CreateBicycleRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/bicycles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("BIC-099"))
                .andExpect(jsonPath("$.type").value("URBANA"))
                .andExpect(jsonPath("$.status").value("DISPONIBLE"));
    }

    @Test
    void shouldReturn400WhenCodeIsBlank() throws Exception {
        CreateBicycleRequest request = new CreateBicycleRequest("", BicycleType.URBANA, null);

        mockMvc.perform(post("/api/bicycles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void shouldReturn400WhenTypeIsNull() throws Exception {
        String body = "{\"code\":\"BIC-099\"}";

        mockMvc.perform(post("/api/bicycles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnAvailableBicyclesWithoutTypeFilter() throws Exception {
        List<BicycleResponse> bikes = List.of(
                new BicycleResponse(1L, "BIC-001", BicycleType.URBANA, BicycleStatus.DISPONIBLE),
                new BicycleResponse(2L, "BIC-002", BicycleType.MONTAÑA, BicycleStatus.DISPONIBLE)
        );
        when(bicycleService.findAvailable(isNull())).thenReturn(bikes);

        mockMvc.perform(get("/api/bicycles/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").value("BIC-001"));
    }

    @Test
    void shouldReturnAvailableBicyclesFilteredByType() throws Exception {
        List<BicycleResponse> bikes = List.of(
                new BicycleResponse(1L, "BIC-001", BicycleType.URBANA, BicycleStatus.DISPONIBLE)
        );
        when(bicycleService.findAvailable(BicycleType.URBANA)).thenReturn(bikes);

        mockMvc.perform(get("/api/bicycles/available")
                        .param("type", "URBANA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("URBANA"));
    }

    @Test
    void shouldReturnEmptyArrayWhenNoBicyclesAvailable() throws Exception {
        when(bicycleService.findAvailable(isNull())).thenReturn(List.of());

        mockMvc.perform(get("/api/bicycles/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
