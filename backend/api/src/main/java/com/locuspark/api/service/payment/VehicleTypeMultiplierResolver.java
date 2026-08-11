package com.locuspark.api.service.payment;

import com.locuspark.api.entity.VehicleTypePriceMultiplier;
import com.locuspark.api.enums.VehicleType;
import com.locuspark.api.repository.VehicleTypePriceMultiplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VehicleTypeMultiplierResolver {

    private final VehicleTypePriceMultiplierRepository multiplierRepository;

    public BigDecimal resolve(UUID companyId, VehicleType vehicleType) {
        if (companyId == null || vehicleType == null) {
            return BigDecimal.ONE;
        }

        return multiplierRepository.findByCompanyIdAndVehicleType(companyId, vehicleType)
                .map(VehicleTypePriceMultiplier::getMultiplier)
                .orElse(BigDecimal.ONE);
    }
}
