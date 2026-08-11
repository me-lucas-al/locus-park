package com.locuspark.api.service;

import com.locuspark.api.dto.request.VehicleTypeMultiplierItemRequest;
import com.locuspark.api.dto.request.VehicleTypePricingBatchRequest;
import com.locuspark.api.dto.response.PricingConfigurationResponse;
import com.locuspark.api.dto.response.TariffConfigurationResponse;
import com.locuspark.api.dto.response.VehicleTypeMultiplierResponse;
import com.locuspark.api.entity.Company;
import com.locuspark.api.entity.PricingConfiguration;
import com.locuspark.api.entity.TariffConfiguration;
import com.locuspark.api.entity.VehicleTypePriceMultiplier;
import com.locuspark.api.enums.VehicleType;
import com.locuspark.api.exception.ResourceNotFoundException;
import com.locuspark.api.mapper.PricingConfigurationMapper;
import com.locuspark.api.mapper.TariffConfigurationMapper;
import com.locuspark.api.repository.CompanyRepository;
import com.locuspark.api.repository.PricingConfigurationRepository;
import com.locuspark.api.repository.TariffConfigurationRepository;
import com.locuspark.api.repository.VehicleTypePriceMultiplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Serviço de Configuração - ConfigurationService")
class ConfigurationServiceTest {

    @Mock
    private TariffConfigurationRepository tariffRepository;

    @Mock
    private PricingConfigurationRepository pricingRepository;

    @Mock
    private VehicleTypePriceMultiplierRepository vehicleTypePriceMultiplierRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private TariffConfigurationMapper tariffMapper;

    @Mock
    private PricingConfigurationMapper pricingMapper;

    @InjectMocks
    private ConfigurationService configurationService;

    private final UUID companyId = UUID.randomUUID();
    private Company company;

    @BeforeEach
    void setUp() {
        company = Company.builder()
                .id(companyId)
                .name("Estacionamento Central")
                .build();
    }

    @Nested
    @DisplayName("Cenários de Configuração de Tarifa (getTariffByCompany)")
    class GetTariffByCompany {

        @Test
        @DisplayName("Deve retornar a tarifa mapeada com sucesso para um companyId válido")
        void getTariffByCompanySuccess() {
            // Arrange
            TariffConfiguration tariff = TariffConfiguration.builder()
                    .id(UUID.randomUUID())
                    .company(company)
                    .toleranceMinutes(15)
                    .firstHourValue(BigDecimal.valueOf(10.00))
                    .additionalFractionValue(BigDecimal.valueOf(2.50))
                    .overnightFee(BigDecimal.valueOf(50.00))
                    .lostTicketFee(BigDecimal.valueOf(20.00))
                    .build();

            TariffConfigurationResponse response = new TariffConfigurationResponse(
                    tariff.getId(),
                    companyId,
                    15,
                    BigDecimal.valueOf(10.00),
                    BigDecimal.valueOf(2.50),
                    BigDecimal.valueOf(50.00),
                    BigDecimal.valueOf(20.00)
            );

            when(tariffRepository.findByCompanyId(companyId)).thenReturn(Optional.of(tariff));
            when(tariffMapper.toResponse(tariff)).thenReturn(response);

            // Act
            TariffConfigurationResponse result = configurationService.getTariffByCompany(companyId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.companyId()).isEqualTo(companyId);
            assertThat(result.toleranceMinutes()).isEqualTo(15);
            assertThat(result.firstHourValue()).isEqualByComparingTo(BigDecimal.valueOf(10.00));
            assertThat(result.additionalFractionValue()).isEqualByComparingTo(BigDecimal.valueOf(2.50));
            assertThat(result.overnightFee()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
            assertThat(result.lostTicketFee()).isEqualByComparingTo(BigDecimal.valueOf(20.00));

            verify(tariffRepository).findByCompanyId(companyId);
            verify(tariffMapper).toResponse(tariff);
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException se a configuração de tarifa não for encontrada")
        void getTariffByCompanyNotFound() {
            // Arrange
            when(tariffRepository.findByCompanyId(companyId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> configurationService.getTariffByCompany(companyId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Configuração tarifária não encontrada para esta empresa.");

            verify(tariffRepository).findByCompanyId(companyId);
            verifyNoInteractions(tariffMapper);
        }
    }

    @Nested
    @DisplayName("Cenários de Configuração de Preço (getPricingByCompany)")
    class GetPricingByCompany {

        @Test
        @DisplayName("Deve retornar a configuração de preço mapeada com sucesso para um companyId válido")
        void getPricingByCompanySuccess() {
            // Arrange
            PricingConfiguration pricing = PricingConfiguration.builder()
                    .id(UUID.randomUUID())
                    .company(company)
                    .dailyTriggerHours(6)
                    .dailyValue(BigDecimal.valueOf(40.00))
                    .monthlyBaseValue(BigDecimal.valueOf(200.00))
                    .build();

            PricingConfigurationResponse response = new PricingConfigurationResponse(
                    pricing.getId(),
                    companyId,
                    6,
                    BigDecimal.valueOf(40.00),
                    BigDecimal.valueOf(200.00)
            );

            when(pricingRepository.findByCompanyId(companyId)).thenReturn(Optional.of(pricing));
            when(pricingMapper.toResponse(pricing)).thenReturn(response);

            // Act
            PricingConfigurationResponse result = configurationService.getPricingByCompany(companyId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.companyId()).isEqualTo(companyId);
            assertThat(result.dailyTriggerHours()).isEqualTo(6);
            assertThat(result.dailyValue()).isEqualByComparingTo(BigDecimal.valueOf(40.00));
            assertThat(result.monthlyBaseValue()).isEqualByComparingTo(BigDecimal.valueOf(200.00));

            verify(pricingRepository).findByCompanyId(companyId);
            verify(pricingMapper).toResponse(pricing);
        }

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException se a configuração de preço não for encontrada")
        void getPricingByCompanyNotFound() {
            // Arrange
            when(pricingRepository.findByCompanyId(companyId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> configurationService.getPricingByCompany(companyId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Configuração de preços não encontrada para esta empresa.");

            verify(pricingRepository).findByCompanyId(companyId);
            verifyNoInteractions(pricingMapper);
        }
    }

    @Nested
    @DisplayName("Cenários de Precificação por Tipo de Veículo")
    class VehiclePricingScenarios {

        @Test
        @DisplayName("Deve retornar multiplicadores para todos os tipos de veículo com padrão 1.00 se não cadastrado")
        void getVehiclePricingReturnsAllTypesWithDefaults() {
            when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
            when(vehicleTypePriceMultiplierRepository.findAllByCompanyId(companyId)).thenReturn(List.of(
                    VehicleTypePriceMultiplier.builder()
                            .id(UUID.randomUUID())
                            .company(company)
                            .vehicleType(VehicleType.MOTORCYCLE)
                            .multiplier(BigDecimal.valueOf(0.60))
                            .build()
            ));

            List<VehicleTypeMultiplierResponse> responses = configurationService.getVehicleTypePricingByCompany(companyId);

            assertThat(responses).hasSize(VehicleType.values().length);
            var motoResponse = responses.stream().filter(r -> r.vehicleType() == VehicleType.MOTORCYCLE).findFirst().orElseThrow();
            assertThat(motoResponse.multiplier()).isEqualByComparingTo(BigDecimal.valueOf(0.60));

            var carResponse = responses.stream().filter(r -> r.vehicleType() == VehicleType.CAR).findFirst().orElseThrow();
            assertThat(carResponse.multiplier()).isEqualByComparingTo(BigDecimal.valueOf(1.00));
        }

        @Test
        @DisplayName("Deve salvar ou atualizar multiplicadores em lote com sucesso")
        void saveOrUpdateVehiclePricingBatch() {
            when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
            when(vehicleTypePriceMultiplierRepository.findByCompanyIdAndVehicleType(companyId, VehicleType.MOTORCYCLE))
                    .thenReturn(Optional.empty());

            VehicleTypePricingBatchRequest request = new VehicleTypePricingBatchRequest(List.of(
                    new VehicleTypeMultiplierItemRequest(VehicleType.MOTORCYCLE, BigDecimal.valueOf(0.50))
            ));

            List<VehicleTypeMultiplierResponse> result = configurationService.saveOrUpdateVehicleTypePricing(companyId, request);

            verify(vehicleTypePriceMultiplierRepository).save(any(VehicleTypePriceMultiplier.class));
            assertThat(result).isNotNull();
        }
    }
}
