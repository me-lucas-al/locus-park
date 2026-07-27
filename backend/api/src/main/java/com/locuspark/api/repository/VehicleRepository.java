package com.locuspark.api.repository;

import com.locuspark.api.entity.Vehicle;
import com.locuspark.api.types.Plate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    boolean existsByPlateAndCompanyId(Plate plate, UUID companyId);
    Optional<Vehicle> findByPlateAndCompanyId(Plate plate, UUID companyId);
    List<Vehicle> findByCompanyId(UUID companyId);
    Optional<Vehicle> findByIdAndCompanyId(UUID id, UUID companyId);
}
