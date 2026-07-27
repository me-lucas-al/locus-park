package com.locuspark.api.service;

import com.locuspark.api.dto.request.VehicleRequest;
import com.locuspark.api.dto.response.VehicleResponse;
import com.locuspark.api.entity.Client;
import com.locuspark.api.entity.Company;
import com.locuspark.api.entity.TariffConfiguration;
import com.locuspark.api.entity.Vehicle;
import com.locuspark.api.enums.VehicleType;
import com.locuspark.api.exception.BusinessException;
import com.locuspark.api.mapper.VehicleMapper;
import com.locuspark.api.repository.ClientRepository;
import com.locuspark.api.repository.CompanyRepository;
import com.locuspark.api.repository.TariffConfigurationRepository;
import com.locuspark.api.repository.VehicleRepository;
import com.locuspark.api.types.Plate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Serviço de Veículo - VehicleService")
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private TariffConfigurationRepository tariffConfigurationRepository;

    @Mock
    private VehicleMapper vehicleMapper;

    @InjectMocks
    private VehicleService vehicleService;

    private final UUID companyId = UUID.randomUUID();
    private final UUID clientId = UUID.randomUUID();
    private final UUID vehicleId = UUID.randomUUID();

    private Company company;
    private Client client;
    private VehicleRequest requestRotativo;
    private VehicleRequest requestMensalista;
    private Vehicle vehicleRotativo;
    private Vehicle vehicleMensalista;
    private VehicleResponse responseRotativo;
    private VehicleResponse responseMensalista;

    @BeforeEach
    void setUp() {
        company = Company.builder()
                .id(companyId)
                .name("Empresa de Teste")
                .build();

        client = Client.builder()
                .id(clientId)
                .name("Cliente Mensalista")
                .company(company)
                .build();

        requestRotativo = new VehicleRequest("AAA1234", "Civic", "Preto", VehicleType.CAR, null);
        requestMensalista = new VehicleRequest("AAA1234", "Civic", "Preto", VehicleType.CAR, clientId);

        Plate plate = new Plate("AAA1234");

        vehicleRotativo = Vehicle.builder()
                .id(vehicleId)
                .plate(plate)
                .model("Civic")
                .color("Preto")
                .company(company)
                .client(null)
                .build();

        vehicleMensalista = Vehicle.builder()
                .id(vehicleId)
                .plate(plate)
                .model("Civic")
                .color("Preto")
                .company(company)
                .client(client)
                .build();

        responseRotativo = new VehicleResponse(
                vehicleId,
                "AAA1234",
                "Civic",
                "Preto",
                VehicleType.CAR,
                null,
                companyId
        );

        responseMensalista = new VehicleResponse(
                vehicleId,
                "AAA1234",
                "Civic",
                "Preto",
                VehicleType.CAR,
                clientId,
                companyId
        );
    }

    @Nested
    @DisplayName("Cenários de Cadastro de Veículos (createVehicle)")
    class CreateVehicle {

        @Test
        @DisplayName("Deve salvar veículo com sucesso para cliente rotativo (clientId nulo)")
        void createVehicleRotativoSuccess() {
            // Arrange
            when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
            when(tariffConfigurationRepository.findByCompanyId(companyId)).thenReturn(Optional.of(new TariffConfiguration()));
            when(vehicleRepository.existsByPlateAndCompanyId(any(Plate.class), eq(companyId))).thenReturn(false);
            when(vehicleMapper.toEntity(requestRotativo)).thenReturn(vehicleRotativo);
            when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicleRotativo);
            when(vehicleMapper.toResponse(vehicleRotativo)).thenReturn(responseRotativo);

            // Act
            VehicleResponse result = vehicleService.createVehicle(companyId, requestRotativo);

            // Assert
            assertNotNull(result);
            assertNull(result.clientId());
            assertEquals(companyId, result.companyId());
            
            verify(companyRepository).findById(companyId);
            verify(tariffConfigurationRepository).findByCompanyId(companyId);
            verify(vehicleRepository).existsByPlateAndCompanyId(any(Plate.class), eq(companyId));
            verify(vehicleRepository).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("Deve salvar veículo com sucesso para cliente mensalista existente")
        void createVehicleMensalistaSuccess() {
            // Arrange
            when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
            when(tariffConfigurationRepository.findByCompanyId(companyId)).thenReturn(Optional.of(new TariffConfiguration()));
            when(clientRepository.findByIdAndCompanyId(clientId, companyId)).thenReturn(Optional.of(client));
            when(vehicleRepository.existsByPlateAndCompanyId(any(Plate.class), eq(companyId))).thenReturn(false);
            when(vehicleMapper.toEntity(requestMensalista)).thenReturn(vehicleMensalista);
            when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicleMensalista);
            when(vehicleMapper.toResponse(vehicleMensalista)).thenReturn(responseMensalista);

            // Act
            VehicleResponse result = vehicleService.createVehicle(companyId, requestMensalista);

            // Assert
            assertNotNull(result);
            assertEquals(clientId, result.clientId());
            assertEquals(companyId, result.companyId());

            verify(companyRepository).findById(companyId);
            verify(tariffConfigurationRepository).findByCompanyId(companyId);
            verify(clientRepository).findByIdAndCompanyId(clientId, companyId);
            verify(vehicleRepository).existsByPlateAndCompanyId(any(Plate.class), eq(companyId));
            verify(vehicleRepository).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("Deve lançar BusinessException se a placa já existir na mesma empresa")
        void createVehicleFailPlateAlreadyExists() {
            // Arrange
            when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
            when(tariffConfigurationRepository.findByCompanyId(companyId)).thenReturn(Optional.of(new TariffConfiguration()));
            when(vehicleRepository.existsByPlateAndCompanyId(any(Plate.class), eq(companyId))).thenReturn(true);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    vehicleService.createVehicle(companyId, requestRotativo)
            );

            assertEquals("Já existe um veículo cadastrado com esta placa nesta empresa.", exception.getMessage());
            verify(companyRepository).findById(companyId);
            verify(tariffConfigurationRepository).findByCompanyId(companyId);
            verify(vehicleRepository).existsByPlateAndCompanyId(any(Plate.class), eq(companyId));
            verify(vehicleRepository, never()).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("Deve lançar BusinessException se a tarifa não estiver configurada")
        void createVehicleFailTariffNotConfigured() {
            // Arrange
            when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
            when(tariffConfigurationRepository.findByCompanyId(companyId)).thenReturn(Optional.empty());

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    vehicleService.createVehicle(companyId, requestRotativo)
            );

            assertEquals("Não é possível registrar veículos porque os preços do estacionamento ainda não foram configurados. Por favor, configure as tarifas de preços primeiro.", exception.getMessage());
            verify(companyRepository).findById(companyId);
            verify(tariffConfigurationRepository).findByCompanyId(companyId);
            verify(vehicleRepository, never()).existsByPlateAndCompanyId(any(Plate.class), eq(companyId));
            verify(vehicleRepository, never()).save(any(Vehicle.class));
        }
    }

    @Nested
    @DisplayName("Cenários de Listagem de Veículos (listAllVehiclesByCompany)")
    class ListAllVehiclesByCompany {

        @Test
        @DisplayName("Deve retornar a lista de veículos cadastrados na empresa")
        void listAllVehiclesSuccess() {
            // Arrange
            List<Vehicle> vehicles = List.of(vehicleRotativo);
            when(vehicleRepository.findByCompanyId(companyId)).thenReturn(vehicles);
            when(vehicleMapper.toResponse(vehicleRotativo)).thenReturn(responseRotativo);

            // Act
            List<VehicleResponse> result = vehicleService.listAllVehiclesByCompany(companyId);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(responseRotativo, result.get(0));

            verify(vehicleRepository).findByCompanyId(companyId);
            verify(vehicleMapper).toResponse(vehicleRotativo);
        }
    }

    @Nested
    @DisplayName("Cenários de Busca de Veículo por ID e Empresa (getVehicleByIdAndCompany)")
    class GetVehicleByIdAndCompany {

        @Test
        @DisplayName("Deve retornar o veículo se pertencer à empresa informada")
        void getVehicleByIdAndCompanySuccess() {
            // Arrange
            when(vehicleRepository.findByIdAndCompanyId(vehicleId, companyId)).thenReturn(Optional.of(vehicleRotativo));
            when(vehicleMapper.toResponse(vehicleRotativo)).thenReturn(responseRotativo);

            // Act
            VehicleResponse result = vehicleService.getVehicleByIdAndCompany(vehicleId, companyId);

            // Assert
            assertNotNull(result);
            assertEquals(vehicleId, result.id());
            assertEquals(companyId, result.companyId());

            verify(vehicleRepository).findByIdAndCompanyId(vehicleId, companyId);
        }

        @Test
        @DisplayName("Deve lançar BusinessException se o veículo pertencer a outra empresa ou não existir")
        void getVehicleByIdAndCompanyFailDifferentCompanyOrNotFound() {
            // Arrange
            when(vehicleRepository.findByIdAndCompanyId(vehicleId, companyId)).thenReturn(Optional.empty());

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () ->
                    vehicleService.getVehicleByIdAndCompany(vehicleId, companyId)
            );

            assertEquals("Veículo não encontrado ou não pertence a esta empresa.", exception.getMessage());
            verify(vehicleRepository).findByIdAndCompanyId(vehicleId, companyId);
            verify(vehicleMapper, never()).toResponse(any(Vehicle.class));
        }
    }
}
