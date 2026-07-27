package com.locuspark.api.service;

import com.locuspark.api.entity.Partnership;
import com.locuspark.api.entity.PricingConfiguration;
import com.locuspark.api.entity.TariffConfiguration;
import com.locuspark.api.entity.Ticket;
import com.locuspark.api.enums.DiscountType;
import com.locuspark.api.service.payment.GrossStayChargeCalculator;
import com.locuspark.api.service.payment.HourlyRateCalculator;
import com.locuspark.api.service.payment.PartnershipDiscountCalculator;
import com.locuspark.api.service.payment.StayCharge;
import com.locuspark.api.service.payment.TolerancePolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes de Serviço de Pagamento - PaymentService")
class PaymentServiceTest {

    private static final ZoneId PATIO_ZONE = ZoneId.of("America/Sao_Paulo");

    private static Instant at(LocalDateTime local) {
        return local.atZone(PATIO_ZONE).toInstant();
    }

    private PaymentService paymentService;
    private TariffConfiguration tariff;
    private PricingConfiguration pricing;

    @BeforeEach
    void setUp() {
        HourlyRateCalculator hourlyRateCalculator = new HourlyRateCalculator();
        paymentService = new PaymentService(
                new TolerancePolicy(),
                new GrossStayChargeCalculator(hourlyRateCalculator),
                new PartnershipDiscountCalculator(hourlyRateCalculator)
        );
        tariff = TariffConfiguration.builder()
                .toleranceMinutes(10)
                .firstHourValue(BigDecimal.valueOf(10))
                .additionalFractionValue(BigDecimal.valueOf(2))
                .build();
        pricing = PricingConfiguration.builder().dailyTriggerHours(24).dailyValue(BigDecimal.valueOf(80)).build();
    }

    @Nested
    @DisplayName("Cenários de Tolerância")
    class ToleranceScenarios {

        @Test
        @DisplayName("Deve retornar StayCharge.free() quando dentro da tolerância, mesmo com parceria FREE_HOURS")
        void toleranceShortCircuitsBeforeDiscount() {
            Partnership partnership = Partnership.builder().discountType(DiscountType.FREE_HOURS).value(BigDecimal.valueOf(1)).build();
            Ticket ticket = Ticket.builder()
                    .enteredAt(at(LocalDateTime.of(2026, 1, 1, 10, 0)))
                    .partnership(partnership)
                    .build();
            Instant exitTime = at(LocalDateTime.of(2026, 1, 1, 10, 5));

            StayCharge charge = paymentService.calculateStayCharge(ticket, exitTime, tariff, pricing);

            assertThat(charge).isEqualTo(StayCharge.free());
        }
    }

    @Nested
    @DisplayName("Cenários de Cobrança")
    class ChargeScenarios {

        @Test
        @DisplayName("Deve calcular bruto igual ao líquido quando não há parceria")
        void grossEqualsNetWithoutPartnership() {
            Ticket ticket = Ticket.builder().enteredAt(at(LocalDateTime.of(2026, 1, 1, 10, 0))).build();
            Instant exitTime = at(LocalDateTime.of(2026, 1, 1, 13, 0));

            StayCharge charge = paymentService.calculateStayCharge(ticket, exitTime, tariff, pricing);

            assertThat(charge.gross()).isEqualByComparingTo(charge.net());
            assertThat(charge.discount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Deve manter gross == net + discount quando há desconto de parceria")
        void grossEqualsNetPlusDiscountWithPartnership() {
            Partnership partnership = Partnership.builder().discountType(DiscountType.PERCENTAGE).value(BigDecimal.valueOf(20)).build();
            Ticket ticket = Ticket.builder()
                    .enteredAt(at(LocalDateTime.of(2026, 1, 1, 10, 0)))
                    .partnership(partnership)
                    .build();
            Instant exitTime = at(LocalDateTime.of(2026, 1, 1, 13, 0));

            StayCharge charge = paymentService.calculateStayCharge(ticket, exitTime, tariff, pricing);

            assertThat(charge.gross()).isEqualByComparingTo(charge.net().add(charge.discount()));
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException quando a saída é anterior à entrada")
        void throwsWhenExitBeforeEntry() {
            Ticket ticket = Ticket.builder().enteredAt(at(LocalDateTime.of(2026, 1, 1, 10, 0))).build();
            Instant exitTime = at(LocalDateTime.of(2026, 1, 1, 9, 0));

            assertThatThrownBy(() -> paymentService.calculateStayCharge(ticket, exitTime, tariff, pricing))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
