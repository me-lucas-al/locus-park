package com.locuspark.api.repository;

import com.locuspark.api.entity.VehicleTypePriceMultiplier;
import com.locuspark.api.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleTypePriceMultiplierRepository extends JpaRepository<VehicleTypePriceMultiplier, UUID> {
    Optional<VehicleTypePriceMultiplier> findByCompanyIdAndVehicleType(UUID companyId, VehicleType vehicleType);
    List<VehicleTypePriceMultiplier> findAllByCompanyId(UUID companyId);
    void deleteAllByCompanyId(UUID companyId);
}
