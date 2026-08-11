package com.locuspark.api.controller;

import com.locuspark.api.dto.request.VehicleTypePricingBatchRequest;
import com.locuspark.api.dto.response.VehicleTypeMultiplierResponse;
import com.locuspark.api.service.ConfigurationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/configurations/vehicle-pricing")
public class VehiclePricingConfigurationController {

    private final ConfigurationService configurationService;

    public VehiclePricingConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @GetMapping
    public ResponseEntity<List<VehicleTypeMultiplierResponse>> getVehiclePricing(@RequestAttribute("companyId") UUID companyId) {
        return ResponseEntity.ok(configurationService.getVehicleTypePricingByCompany(companyId));
    }

    @PutMapping
    public ResponseEntity<List<VehicleTypeMultiplierResponse>> updateVehiclePricing(
            @RequestAttribute("companyId") UUID companyId,
            @RequestBody @Valid VehicleTypePricingBatchRequest request) {
        return ResponseEntity.ok(configurationService.saveOrUpdateVehicleTypePricing(companyId, request));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteVehiclePricing(@RequestAttribute("companyId") UUID companyId) {
        configurationService.deleteVehicleTypePricing(companyId);
        return ResponseEntity.noContent().build();
    }
}
