package com.locuspark.api.repository;

import com.locuspark.api.entity.Client;
import com.locuspark.api.entity.Company;
import com.locuspark.api.entity.Ticket;
import com.locuspark.api.entity.Vehicle;
import com.locuspark.api.enums.ClientType;
import com.locuspark.api.enums.CompanyStatus;
import com.locuspark.api.enums.TicketStatus;
import com.locuspark.api.enums.VehicleType;
import com.locuspark.api.service.report.TicketRecord;
import com.locuspark.api.types.Cnpj;
import com.locuspark.api.types.Cpf;
import com.locuspark.api.types.Plate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Testes de Projeção de Tickets - TicketRepository")
class TicketRecordProjectionTest {

    private static final ZoneId PATIO_ZONE = ZoneId.of("America/Sao_Paulo");

    private static Instant at(LocalDateTime local) {
        return local.atZone(PATIO_ZONE).toInstant();
    }

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TicketRepository ticketRepository;

    private Company persistCompany(String cnpj) {
        Company company = Company.builder()
                .name("Estacionamento Central")
                .cnpj(new Cnpj(cnpj))
                .totalSpots(50)
                .status(CompanyStatus.ACTIVE)
                .build();
        return entityManager.persistAndFlush(company);
    }

    private Vehicle persistVehicle(Company company, String plate, Client client) {
        Vehicle vehicle = Vehicle.builder()
                .plate(new Plate(plate))
                .model("Gol")
                .color("Prata")
                .company(company)
                .type(VehicleType.CAR)
                .client(client)
                .build();
        return entityManager.persistAndFlush(vehicle);
    }

    private Ticket persistTicket(Company company, Vehicle vehicle, TicketStatus status, LocalDateTime enteredAt, LocalDateTime exitedAt) {
        Ticket ticket = Ticket.builder()
                .company(company)
                .vehicle(vehicle)
                .status(status)
                .enteredAt(at(enteredAt))
                .exitedAt(exitedAt != null ? at(exitedAt) : null)
                .totalAmount(exitedAt != null ? java.math.BigDecimal.valueOf(50) : null)
                .build();
        return entityManager.persistAndFlush(ticket);
    }

    @Test
    @DisplayName("Deve materializar Plate e Cpf convertidos na projeção")
    void materializesConvertedValueTypes() {
        Company company = persistCompany("11444777000161");
        Client client = entityManager.persistAndFlush(
                Client.builder().name("Maria Silva").cpf(new Cpf("12345678909")).phone("11999990000").type(ClientType.AVULSO).company(company).build());
        Vehicle vehicle = persistVehicle(company, "ABC1234", client);
        persistTicket(company, vehicle, TicketStatus.PAID,
                LocalDateTime.of(2026, 1, 10, 8, 0), LocalDateTime.of(2026, 1, 10, 9, 0));

        List<TicketRecord> result = ticketRepository.findPaidRecordsByExitWindow(company.getId(),
                at(LocalDateTime.of(2026, 1, 1, 0, 0)), at(LocalDateTime.of(2026, 2, 1, 0, 0)));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).plate()).isEqualTo(new Plate("ABC1234"));
        assertThat(result.get(0).clientCpf()).isEqualTo(new Cpf("12345678909"));
    }

    @Test
    @DisplayName("Left join não deve eliminar tickets sem cliente vinculado")
    void leftJoinKeepsTicketsWithoutClient() {
        Company company = persistCompany("22444777000188");
        Vehicle vehicle = persistVehicle(company, "XYZ9876", null);
        persistTicket(company, vehicle, TicketStatus.PAID,
                LocalDateTime.of(2026, 1, 10, 8, 0), LocalDateTime.of(2026, 1, 10, 9, 0));

        List<TicketRecord> result = ticketRepository.findPaidRecordsByExitWindow(company.getId(),
                at(LocalDateTime.of(2026, 1, 1, 0, 0)), at(LocalDateTime.of(2026, 2, 1, 0, 0)));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).clientId()).isNull();
        assertThat(result.get(0).clientName()).isNull();
    }

    @Test
    @DisplayName("Não deve retornar tickets de outra empresa (isolamento de tenant)")
    void isolatesTenants() {
        Company companyA = persistCompany("33444777000102");
        Company companyB = persistCompany("44444777000110");
        Vehicle vehicleA = persistVehicle(companyA, "AAA1111", null);
        Vehicle vehicleB = persistVehicle(companyB, "BBB2222", null);
        persistTicket(companyA, vehicleA, TicketStatus.PAID, LocalDateTime.of(2026, 1, 10, 8, 0), LocalDateTime.of(2026, 1, 10, 9, 0));
        persistTicket(companyB, vehicleB, TicketStatus.PAID, LocalDateTime.of(2026, 1, 10, 8, 0), LocalDateTime.of(2026, 1, 10, 9, 0));

        List<TicketRecord> result = ticketRepository.findPaidRecordsByExitWindow(companyA.getId(),
                at(LocalDateTime.of(2026, 1, 1, 0, 0)), at(LocalDateTime.of(2026, 2, 1, 0, 0)));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).plate()).isEqualTo(new Plate("AAA1111"));
    }

    @Test
    @DisplayName("Janela de saída deve ser inclusiva no início e exclusiva no fim")
    void exitWindowIsLowerInclusiveUpperExclusive() {
        Company company = persistCompany("55444777000137");
        Vehicle vehicle = persistVehicle(company, "INC0001", null);
        persistTicket(company, vehicle, TicketStatus.PAID,
                LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 1, 1, 0, 0));
        persistTicket(company, vehicle, TicketStatus.PAID,
                LocalDateTime.of(2026, 1, 31, 0, 0), LocalDateTime.of(2026, 2, 1, 0, 0));

        List<TicketRecord> result = ticketRepository.findPaidRecordsByExitWindow(company.getId(),
                at(LocalDateTime.of(2026, 1, 1, 0, 0)), at(LocalDateTime.of(2026, 2, 1, 0, 0)));

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("countPresentAt deve contar estadia que atravessa toda a janela")
    void countPresentAtCountsStayCrossingWholeWindow() {
        Company company = persistCompany("66444777000153");
        Vehicle vehicle = persistVehicle(company, "CRO0001", null);
        persistTicket(company, vehicle, TicketStatus.ACTIVE, LocalDateTime.of(2025, 12, 1, 0, 0), null);

        long presentAtStart = ticketRepository.countPresentAt(company.getId(), at(LocalDateTime.of(2026, 1, 1, 0, 0)));

        assertThat(presentAtStart).isEqualTo(1);
    }

    @Test
    @DisplayName("Janela de entrada deve retornar apenas tickets com entrada no intervalo, independente de status")
    void entryWindowIgnoresStatus() {
        Company company = persistCompany("77444777000170");
        Vehicle vehicle = persistVehicle(company, "ENT0001", null);
        persistTicket(company, vehicle, TicketStatus.ACTIVE, LocalDateTime.of(2026, 1, 15, 8, 0), null);

        List<TicketRecord> result = ticketRepository.findRecordsByEntryWindow(company.getId(),
                at(LocalDateTime.of(2026, 1, 1, 0, 0)), at(LocalDateTime.of(2026, 2, 1, 0, 0)));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(TicketStatus.ACTIVE);
    }
}
