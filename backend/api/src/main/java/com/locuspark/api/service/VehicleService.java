package com.locuspark.api.service;

import com.locuspark.api.dto.request.VehicleRequest;
import com.locuspark.api.dto.response.VehicleResponse;
import com.locuspark.api.entity.Client;
import com.locuspark.api.entity.Company;
import com.locuspark.api.entity.Vehicle;
import com.locuspark.api.enums.TicketStatus;
import com.locuspark.api.exception.BusinessException;
import com.locuspark.api.mapper.VehicleMapper;
import com.locuspark.api.repository.ClientRepository;
import com.locuspark.api.repository.CompanyRepository;
import com.locuspark.api.repository.TariffConfigurationRepository;
import com.locuspark.api.repository.TicketRepository;
import com.locuspark.api.repository.VehicleRepository;
import com.locuspark.api.types.Plate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CompanyRepository companyRepository;
    private final ClientRepository clientRepository;
    private final TariffConfigurationRepository tariffConfigurationRepository;
    private final TicketRepository ticketRepository;
    private final VehicleMapper vehicleMapper;

    @Transactional
    public VehicleResponse createVehicle(UUID companyId, VehicleRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException("Empresa não encontrada."));

        // Verifica se a tarifa está configurada para a empresa
        boolean tariffConfigured = tariffConfigurationRepository.findByCompanyId(companyId).isPresent();
        if (!tariffConfigured) {
            throw new BusinessException("Não é possível registrar veículos porque os preços do estacionamento ainda não foram configurados. Por favor, configure as tarifas de preços primeiro.");
        }

        Plate plate = new Plate(request.plate());
        Optional<Vehicle> existingVehicle = vehicleRepository.findByPlateAndCompanyId(plate, companyId);
        if (existingVehicle.isPresent()) {
            boolean hasActiveTicket = ticketRepository.existsByVehiclePlateAndCompanyIdAndStatus(
                    plate, companyId, TicketStatus.ACTIVE);
            if (hasActiveTicket) {
                throw new BusinessException("Já existe um veículo cadastrado com esta placa nesta empresa e ele está no estacionamento.");
            }
        }

        Client client = null;
        if (request.clientId() != null) {
            client = clientRepository.findByIdAndCompanyId(request.clientId(), companyId)
                    .orElseThrow(() -> new BusinessException("Cliente não encontrado ou não pertence a esta empresa."));
        }

        // Se o veículo já esteve cadastrado antes mas já saiu do pátio, reaproveita o registro
        // em vez de tentar inserir outro (evita violar a constraint única de placa+empresa).
        if (existingVehicle.isPresent()) {
            Vehicle vehicle = existingVehicle.get();
            vehicle.setModel(request.model());
            vehicle.setColor(request.color());
            vehicle.setType(request.type());
            vehicle.setClient(client);

            Vehicle updatedVehicle = vehicleRepository.save(vehicle);
            return vehicleMapper.toResponse(updatedVehicle);
        }

        Vehicle vehicle = vehicleMapper.toEntity(request);
        vehicle.setCompany(company);
        vehicle.setClient(client);

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return vehicleMapper.toResponse(savedVehicle);
    }

    public List<VehicleResponse> listAllVehiclesByCompany(UUID companyId) {
        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);
        return vehicles.stream()
                .map(vehicleMapper::toResponse)
                .toList();
    }

    public VehicleResponse getVehicleByIdAndCompany(UUID id, UUID companyId) {
        Vehicle vehicle = vehicleRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new BusinessException("Veículo não encontrado ou não pertence a esta empresa."));
        return vehicleMapper.toResponse(vehicle);
    }
    @Transactional
    public VehicleResponse updateVehicle(UUID id, UUID companyId, VehicleRequest request) {
        // Garante que o veículo pertence à empresa antes de atualizar
        Vehicle vehicle = vehicleRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new BusinessException("Veículo não encontrado ou não pertence a esta empresa."));

        // Se a placa mudou, checa se a nova já não está em uso por outro carro da mesma empresa
        Plate newPlate = new Plate(request.plate());
        if (!vehicle.getPlate().equals(newPlate) && vehicleRepository.existsByPlateAndCompanyId(newPlate, companyId)) {
            throw new BusinessException("Já existe um veículo cadastrado com esta placa nesta empresa.");
        }

        // Atualiza o vínculo do cliente, se houver
        Client client = null;
        if (request.clientId() != null) {
            client = clientRepository.findByIdAndCompanyId(request.clientId(), companyId)
                    .orElseThrow(() -> new BusinessException("Cliente não encontrado ou não pertence a esta empresa."));
        }

        // Atualiza os dados da entidade
        vehicle.setPlate(newPlate);
        vehicle.setModel(request.model());
        vehicle.setColor(request.color());
        vehicle.setType(request.type()); // ex: CAR, MOTORCYCLE, se houver Enums
        vehicle.setClient(client);

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return vehicleMapper.toResponse(updatedVehicle);
    }

    @Transactional
    public void deleteVehicle(UUID id, UUID companyId) {
        Vehicle vehicle = vehicleRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new BusinessException("Veículo não encontrado ou não pertence a esta empresa."));

        boolean hasTickets = ticketRepository.existsByVehiclePlateAndCompanyId(vehicle.getPlate(), companyId);
        if (hasTickets) {
            throw new BusinessException("Não é possível remover este veículo pois ele possui histórico de tickets. Remova os tickets vinculados antes de prosseguir.");
        }

        vehicleRepository.delete(vehicle);
    }
}
