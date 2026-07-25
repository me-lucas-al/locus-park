package com.locuspark.api.service;

import com.locuspark.api.dto.response.TicketResponse;
import com.locuspark.api.entity.Company;
import com.locuspark.api.entity.Ticket;
import com.locuspark.api.enums.TicketStatus;
import com.locuspark.api.exception.ResourceNotFoundException;
import com.locuspark.api.mapper.TicketMapper;
import com.locuspark.api.repository.*;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Serviço de Ticket - TicketService")
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private PartnershipRepository partnershipRepository;
    @Mock
    private PricingConfigurationRepository pricingRepository;
    @Mock
    private PaymentService paymentService;
    @Mock
    private TicketMapper ticketMapper;
    @Mock
    private TariffConfigurationRepository tariffConfigurationRepository;

    @InjectMocks
    private TicketService ticketService;

    private final UUID companyId = UUID.randomUUID();
    private Company company;

    @BeforeEach
    void setUp() {
        company = Company.builder().id(companyId).name("Estacionamento Central").build();
    }

    @Nested
    @DisplayName("Cenários de Listagem por Empresa (listAllTicketsByCompany)")
    class ListAllTicketsByCompany {

        @Test
        @DisplayName("Deve consultar apenas tickets da empresa informada, sem carregar outros tenants")
        void listAllByCompanyFiltersAtQueryLevel() {
            Ticket ticket = Ticket.builder().id(UUID.randomUUID()).company(company).build();
            TicketResponse response = mock(TicketResponse.class);

            when(ticketRepository.findAllByCompanyId(companyId)).thenReturn(List.of(ticket));
            when(ticketMapper.toResponse(ticket)).thenReturn(response);

            List<TicketResponse> result = ticketService.listAllTicketsByCompany(companyId);

            assertThat(result).containsExactly(response);
            verify(ticketRepository).findAllByCompanyId(companyId);
            verify(ticketRepository, never()).findAll();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando a empresa não possui tickets")
        void listAllByCompanyEmpty() {
            when(ticketRepository.findAllByCompanyId(companyId)).thenReturn(List.of());

            List<TicketResponse> result = ticketService.listAllTicketsByCompany(companyId);

            assertThat(result).isEmpty();
            verifyNoInteractions(ticketMapper);
        }
    }

    @Nested
    @DisplayName("Cenários de Busca por Id e Empresa (getTicketByIdAndCompany)")
    class GetTicketByIdAndCompany {

        @Test
        @DisplayName("Deve lançar ResourceNotFoundException quando o ticket não pertence à empresa")
        void getByIdNotFound() {
            UUID ticketId = UUID.randomUUID();
            when(ticketRepository.findByIdAndCompanyId(ticketId, companyId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ticketService.getTicketByIdAndCompany(ticketId, companyId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Cenários de Exclusão (deleteTicket)")
    class DeleteTicket {

        @Test
        @DisplayName("Deve deletar o ticket quando ele pertence à empresa")
        void deleteTicketSuccess() {
            UUID ticketId = UUID.randomUUID();
            Ticket ticket = Ticket.builder().id(ticketId).company(company).status(TicketStatus.PAID).build();
            when(ticketRepository.findByIdAndCompanyId(ticketId, companyId)).thenReturn(Optional.of(ticket));

            ticketService.deleteTicket(ticketId, companyId);

            verify(ticketRepository).delete(ticket);
        }
    }
}
