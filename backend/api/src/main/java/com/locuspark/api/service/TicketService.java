package com.locuspark.api.service;

import com.locuspark.api.dto.response.TicketResponse;
import com.locuspark.api.entity.*;
import com.locuspark.api.enums.PaymentMethod;
import com.locuspark.api.enums.TicketStatus;
import com.locuspark.api.exception.BusinessException;
import com.locuspark.api.exception.ResourceNotFoundException; // Nova
import com.locuspark.api.mapper.TicketMapper;
import com.locuspark.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CompanyRepository companyRepository;
    private final VehicleRepository vehicleRepository;
    private final PartnershipRepository partnershipRepository; // Se mudou de agreement
    private final PricingConfigurationRepository pricingRepository; // Se mudou de plan
    private final PaymentService paymentService;
    private final TicketMapper ticketMapper;
    private final TariffConfigurationRepository tariffConfigurationRepository;

    @Transactional
    public TicketResponse checkIn(UUID companyId, UUID vehicleId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada."));

        Vehicle vehicle = vehicleRepository.findByIdAndCompanyId(vehicleId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não cadastrado nesta empresa."));

        boolean hasActiveTicket = ticketRepository.existsByVehiclePlateAndCompanyIdAndStatus(
                vehicle.getPlate(), companyId, TicketStatus.ACTIVE);
        if (hasActiveTicket) {
            throw new BusinessException("Este veículo já possui um ticket ativo no pátio.");
        }

        long occupiedSpots = ticketRepository.countByCompanyIdAndStatus(companyId, TicketStatus.ACTIVE);
        if (occupiedSpots >= company.getTotalSpots()) {
            throw new BusinessException("O pátio está lotado. Não há vagas disponíveis.");
        }

        Ticket ticket = Ticket.builder()
                .company(company)
                .vehicle(vehicle)
                .enteredAt(LocalDateTime.now())
                .status(TicketStatus.ACTIVE)
                .build();

        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse checkOut(UUID companyId, UUID ticketId, PaymentMethod paymentMethod) {
        Ticket ticket = ticketRepository.findByIdAndCompanyId(ticketId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket não encontrado nesta empresa."));

        if (ticket.getStatus() == TicketStatus.PAID) {
            throw new BusinessException("Este ticket já foi encerrado e pago.");
        }

        LocalDateTime exitTime = LocalDateTime.now();
        ticket.setExitedAt(exitTime);

        TariffConfiguration tariff = tariffConfigurationRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Configuração de tarifa não encontrada para esta empresa."));

        PricingConfiguration pricing = pricingRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Configuração de preços/planos não encontrada para esta empresa."));

        BigDecimal total = paymentService.calculateStayAmount(ticket, exitTime, tariff, pricing);

        ticket.setTotalAmount(total);
        ticket.setStatus(TicketStatus.PAID);
        ticket.setPaymentMethod(paymentMethod);

        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    // Adicione estes métodos dentro da classe TicketService

    public List<TicketResponse> listAllTicketsByCompany(UUID companyId) {
        return ticketRepository.findAllByCompanyId(companyId).stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    public TicketResponse getTicketByIdAndCompany(UUID id, UUID companyId) {
        Ticket ticket = ticketRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket não encontrado ou não pertence a esta empresa."));
        return ticketMapper.toResponse(ticket);
    }

    @Transactional
    public void deleteTicket(UUID id, UUID companyId) {
        Ticket ticket = ticketRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket não encontrado ou não pertence a esta empresa."));
        ticketRepository.delete(ticket);
    }

    @Transactional
    public TicketResponse applyPartnershipToTicket(UUID ticketId, UUID companyId, UUID partnershipId) {
        Ticket ticket = ticketRepository.findByIdAndCompanyId(ticketId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket não encontrado nesta empresa."));

        Partnership partnership = partnershipRepository.findById(partnershipId)
                .filter(p -> p.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new ResourceNotFoundException("Convênio não encontrado ou pertence a outra empresa."));

        ticket.setPartnership(partnership);
        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }
}