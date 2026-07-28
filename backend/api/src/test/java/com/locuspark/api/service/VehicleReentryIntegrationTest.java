package com.locuspark.api.service;

import com.locuspark.api.dto.request.VehicleRequest;
import com.locuspark.api.dto.response.TicketResponse;
import com.locuspark.api.dto.response.VehicleResponse;
import com.locuspark.api.entity.Company;
import com.locuspark.api.entity.PricingConfiguration;
import com.locuspark.api.entity.TariffConfiguration;
import com.locuspark.api.enums.CompanyStatus;
import com.locuspark.api.enums.PaymentMethod;
import com.locuspark.api.enums.VehicleType;
import com.locuspark.api.exception.BusinessException;
import com.locuspark.api.repository.CompanyRepository;
import com.locuspark.api.repository.PricingConfigurationRepository;
import com.locuspark.api.repository.TariffConfigurationRepository;
import com.locuspark.api.types.Cnpj;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Teste de integração ponta a ponta (Spring context real + banco H2) que reproduz o
 * cenário reportado: registrar o mesmo veículo, dar entrada, tentar recadastrar
 * (deve falhar pois ele ainda está no pátio), liberar o veículo e recadastrar
 * a mesma placa novamente (deve funcionar).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Teste de Integração - Reentrada de veículo com a mesma placa")
class VehicleReentryIntegrationTest {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private TariffConfigurationRepository tariffConfigurationRepository;

    @Autowired
    private PricingConfigurationRepository pricingConfigurationRepository;

    private Company company;
    private VehicleRequest request;

    @BeforeEach
    void setUp() {
        company = companyRepository.save(Company.builder()
                .name("Estacionamento de Teste")
                .cnpj(new Cnpj("06990590000123"))
                .totalSpots(10)
                .status(CompanyStatus.ACTIVE)
                .build());

        tariffConfigurationRepository.save(TariffConfiguration.builder()
                .company(company)
                .build());

        pricingConfigurationRepository.save(PricingConfiguration.builder()
                .company(company)
                .dailyTriggerHours(6)
                .dailyValue(new BigDecimal("50.00"))
                .monthlyBaseValue(new BigDecimal("300.00"))
                .build());

        request = new VehicleRequest("ABC1234", "Civic", "Preto", VehicleType.CAR, null);
    }

    @Test
    @DisplayName("Deve bloquear o recadastro enquanto o veículo estiver no pátio, e permitir após a liberação")
    void allowsSamePlateRegistrationOnlyAfterVehicleLeavesParkingLot() {
        VehicleResponse vehicle = vehicleService.createVehicle(company.getId(), request);
        TicketResponse ticket = ticketService.checkIn(company.getId(), vehicle.id());

        assertThatThrownBy(() -> vehicleService.createVehicle(company.getId(), request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Já existe um veículo cadastrado com esta placa nesta empresa e ele está no estacionamento.");

        ticketService.checkOut(company.getId(), ticket.id(), PaymentMethod.DINHEIRO);

        VehicleResponse reRegistered = vehicleService.createVehicle(company.getId(), request);
        assertThat(reRegistered.id()).isEqualTo(vehicle.id());

        TicketResponse newTicket = ticketService.checkIn(company.getId(), reRegistered.id());
        assertThat(newTicket).isNotNull();
        assertThat(newTicket.id()).isNotEqualTo(ticket.id());
    }

    @Test
    @DisplayName("Deve continuar bloqueando placas duplicadas para veículos diferentes ainda no pátio")
    void stillBlocksDuplicatePlateWhileVehicleIsOnPremises() {
        VehicleResponse vehicle = vehicleService.createVehicle(company.getId(), request);
        ticketService.checkIn(company.getId(), vehicle.id());

        VehicleRequest duplicate = new VehicleRequest("ABC1234", "Corolla", "Prata", VehicleType.CAR, null);

        assertThatThrownBy(() -> vehicleService.createVehicle(company.getId(), duplicate))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Já existe um veículo cadastrado com esta placa nesta empresa e ele está no estacionamento.");
    }
}
