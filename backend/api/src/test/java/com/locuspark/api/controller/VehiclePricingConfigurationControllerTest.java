package com.locuspark.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.locuspark.api.dto.request.VehicleTypeMultiplierItemRequest;
import com.locuspark.api.dto.request.VehicleTypePricingBatchRequest;
import com.locuspark.api.dto.response.VehicleTypeMultiplierResponse;
import com.locuspark.api.enums.VehicleType;
import com.locuspark.api.repository.UserRepository;
import com.locuspark.api.security.TokenService;
import com.locuspark.api.service.ConfigurationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehiclePricingConfigurationController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Testes de VehiclePricingConfigurationController")
class VehiclePricingConfigurationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConfigurationService configurationService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    private final UUID companyId = UUID.randomUUID();

    @Nested
    @DisplayName("GET /configurations/vehicle-pricing")
    class GetVehiclePricing {

        @Test
        @DisplayName("Deve retornar lista de multiplicadores da empresa")
        void returnsMultipliersList() throws Exception {
            List<VehicleTypeMultiplierResponse> responseList = List.of(
                    new VehicleTypeMultiplierResponse(UUID.randomUUID(), VehicleType.CAR, BigDecimal.valueOf(1.00), "Carro"),
                    new VehicleTypeMultiplierResponse(UUID.randomUUID(), VehicleType.MOTORCYCLE, BigDecimal.valueOf(0.60), "Moto")
            );

            when(configurationService.getVehicleTypePricingByCompany(companyId)).thenReturn(responseList);

            mockMvc.perform(get("/configurations/vehicle-pricing")
                            .requestAttr("companyId", companyId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].vehicleType").value("CAR"))
                    .andExpect(jsonPath("$[0].multiplier").value(1.00))
                    .andExpect(jsonPath("$[1].vehicleType").value("MOTORCYCLE"))
                    .andExpect(jsonPath("$[1].multiplier").value(0.60));

            verify(configurationService).getVehicleTypePricingByCompany(companyId);
        }
    }

    @Nested
    @DisplayName("PUT /configurations/vehicle-pricing")
    class UpdateVehiclePricing {

        @Test
        @DisplayName("Deve atualizar multiplicadores em lote e retornar HTTP 200")
        void updatesMultipliersBatch() throws Exception {
            VehicleTypePricingBatchRequest request = new VehicleTypePricingBatchRequest(List.of(
                    new VehicleTypeMultiplierItemRequest(VehicleType.CAR, BigDecimal.valueOf(1.00)),
                    new VehicleTypeMultiplierItemRequest(VehicleType.MOTORCYCLE, BigDecimal.valueOf(0.50))
            ));

            List<VehicleTypeMultiplierResponse> updatedList = List.of(
                    new VehicleTypeMultiplierResponse(UUID.randomUUID(), VehicleType.CAR, BigDecimal.valueOf(1.00), "Carro"),
                    new VehicleTypeMultiplierResponse(UUID.randomUUID(), VehicleType.MOTORCYCLE, BigDecimal.valueOf(0.50), "Moto")
            );

            when(configurationService.saveOrUpdateVehicleTypePricing(eq(companyId), any(VehicleTypePricingBatchRequest.class)))
                    .thenReturn(updatedList);

            mockMvc.perform(put("/configurations/vehicle-pricing")
                            .requestAttr("companyId", companyId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[1].vehicleType").value("MOTORCYCLE"))
                    .andExpect(jsonPath("$[1].multiplier").value(0.50));

            verify(configurationService).saveOrUpdateVehicleTypePricing(eq(companyId), any(VehicleTypePricingBatchRequest.class));
        }
    }

    @Nested
    @DisplayName("DELETE /configurations/vehicle-pricing")
    class DeleteVehiclePricing {

        @Test
        @DisplayName("Deve deletar multiplicadores customizados e retornar HTTP 204 No Content")
        void deletesMultipliers() throws Exception {
            mockMvc.perform(delete("/configurations/vehicle-pricing")
                            .requestAttr("companyId", companyId))
                    .andExpect(status().isNoContent());

            verify(configurationService).deleteVehicleTypePricing(companyId);
        }
    }
}
