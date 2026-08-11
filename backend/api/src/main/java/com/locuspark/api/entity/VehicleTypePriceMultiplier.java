package com.locuspark.api.entity;

import com.locuspark.api.enums.VehicleType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "vehicle_type_price_multipliers", uniqueConstraints = {
    @UniqueConstraint(name = "uc_vehicle_type_price_multipliers", columnNames = {"company_id", "vehicle_type"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class VehicleTypePriceMultiplier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 50)
    private VehicleType vehicleType;

    @Column(name = "multiplier", nullable = false, precision = 5, scale = 2)
    private BigDecimal multiplier;
}
