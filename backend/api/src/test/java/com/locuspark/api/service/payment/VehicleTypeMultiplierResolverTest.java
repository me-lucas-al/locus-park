package com.locuspark.api.service.payment;

import com.locuspark.api.entity.Company;
import com.locuspark.api.entity.VehicleTypePriceMultiplier;
import com.locuspark.api.enums.VehicleType;
import com.locuspark.api.repository.VehicleTypePriceMultiplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de VehicleTypeMultiplierResolver")
class VehicleTypeMultiplierResolverTest {

    @Mock
    private VehicleTypePriceMultiplierRepository multiplierRepository;

    @InjectMocks
    private VehicleTypeMultiplierResolver resolver;

    private final UUID companyId = UUID.randomUUID();

    @Test
    @DisplayName("Deve retornar multiplicador cadastrado para o tipo de veículo")
    void returnsRegisteredMultiplier() {
        VehicleTypePriceMultiplier entity = VehicleTypePriceMultiplier.builder()
                .company(Company.builder().id(companyId).build())
                .vehicleType(VehicleType.MOTORCYCLE)
                .multiplier(BigDecimal.valueOf(0.60))
                .build();

        when(multiplierRepository.findByCompanyIdAndVehicleType(companyId, VehicleType.MOTORCYCLE))
                .thenReturn(Optional.of(entity));

        BigDecimal result = resolver.resolve(companyId, VehicleType.MOTORCYCLE);

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(0.60));
    }

    @Test
    @DisplayName("Deve retornar 1.00 como fallback quando não houver registro")
    void returnsOneWhenNotRegistered() {
        when(multiplierRepository.findByCompanyIdAndVehicleType(companyId, VehicleType.VAN))
                .thenReturn(Optional.empty());

        BigDecimal result = resolver.resolve(companyId, VehicleType.VAN);

        assertThat(result).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    @DisplayName("Deve retornar 1.00 se companyId ou vehicleType forem nulos")
    void returnsOneWhenNullParameters() {
        assertThat(resolver.resolve(null, VehicleType.CAR)).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(resolver.resolve(companyId, null)).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(resolver.resolve(null, null)).isEqualByComparingTo(BigDecimal.ONE);
    }
}
