package com.locuspark.api.service.report;

import com.locuspark.api.dto.response.report.VehicleTypeSummaryResponse;
import com.locuspark.api.enums.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes de VehicleTypeSummaryCalculator")
class VehicleTypeSummaryCalculatorTest {

    private final VehicleTypeSummaryCalculator calculator = new VehicleTypeSummaryCalculator(new SharePercentCalculator());

    @Test
    @DisplayName("Deve emitir todos os tipos de veiculo, inclusive os zerados")
    void emitsAllVehicleTypesEvenWhenZeroed() {
        TicketRecord ticket = TicketRecordFixture.paid(LocalDateTime.of(2026, 1, 1, 8, 0), LocalDateTime.of(2026, 1, 1, 9, 0));
        TicketWindow window = TicketWindow.of(List.of(ticket), List.of(), 0);

        List<VehicleTypeSummaryResponse> result = calculator.calculate(window);

        assertThat(result).hasSize(VehicleType.values().length);
        assertThat(result).anySatisfy(summary -> {
            assertThat(summary.type()).isEqualTo(VehicleType.TRUCK);
            assertThat(summary.ticketCount()).isZero();
        });
    }

    @Test
    @DisplayName("Deve contabilizar receita e contagem para o tipo de veiculo do ticket")
    void aggregatesRevenueAndCountForTicketVehicleType() {
        TicketRecord ticket = TicketRecordFixture.paid(LocalDateTime.of(2026, 1, 1, 8, 0), LocalDateTime.of(2026, 1, 1, 9, 0));
        TicketWindow window = TicketWindow.of(List.of(ticket), List.of(), 0);

        List<VehicleTypeSummaryResponse> result = calculator.calculate(window);

        VehicleTypeSummaryResponse car = result.stream().filter(r -> r.type() == VehicleType.CAR).findFirst().orElseThrow();
        assertThat(car.ticketCount()).isEqualTo(1);
        assertThat(car.revenue()).isEqualByComparingTo(java.math.BigDecimal.valueOf(80));
    }
}
