package com.locuspark.api.service.payment;

import com.locuspark.api.entity.PricingConfiguration;
import com.locuspark.api.entity.TariffConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes de GrossStayChargeCalculator")
class GrossStayChargeCalculatorTest {

    private GrossStayChargeCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new GrossStayChargeCalculator(new HourlyRateCalculator());
    }

    @Test
    @DisplayName("Deve cobrar diaria quando horas atingem o gatilho configurado")
    void chargesDailyValueWhenHoursReachTrigger() {
        TariffConfiguration tariff = TariffConfiguration.builder().firstHourValue(BigDecimal.valueOf(10)).build();
        PricingConfiguration pricing = PricingConfiguration.builder()
                .dailyTriggerHours(6)
                .dailyValue(BigDecimal.valueOf(50))
                .build();

        BigDecimal gross = calculator.grossAmount(6 * 60, false, tariff, pricing);

        assertThat(gross).isEqualByComparingTo(BigDecimal.valueOf(50));
    }

    @Test
    @DisplayName("Deve cobrar por hora quando abaixo do gatilho de diaria")
    void chargesHourlyWhenBelowDailyTrigger() {
        TariffConfiguration tariff = TariffConfiguration.builder()
                .firstHourValue(BigDecimal.valueOf(10))
                .additionalFractionValue(BigDecimal.valueOf(2))
                .build();
        PricingConfiguration pricing = PricingConfiguration.builder().dailyTriggerHours(6).dailyValue(BigDecimal.valueOf(50)).build();

        BigDecimal gross = calculator.grossAmount(3 * 60, false, tariff, pricing);

        assertThat(gross).isEqualByComparingTo(BigDecimal.valueOf(14));
    }

    @Test
    @DisplayName("Deve somar a taxa de pernoite quando houver virada de dia")
    void addsOvernightFeeWhenCrossedDate() {
        TariffConfiguration tariff = TariffConfiguration.builder()
                .firstHourValue(BigDecimal.valueOf(10))
                .overnightFee(BigDecimal.valueOf(5))
                .build();
        PricingConfiguration pricing = PricingConfiguration.builder().dailyTriggerHours(24).dailyValue(BigDecimal.valueOf(50)).build();

        BigDecimal gross = calculator.grossAmount(30, true, tariff, pricing);

        assertThat(gross).isEqualByComparingTo(BigDecimal.valueOf(15));
    }

    @Test
    @DisplayName("Deve tratar gatilho de diaria nulo como 24 horas")
    void treatsNullDailyTriggerAsTwentyFourHours() {
        TariffConfiguration tariff = TariffConfiguration.builder().firstHourValue(BigDecimal.valueOf(10)).build();
        PricingConfiguration pricing = PricingConfiguration.builder().dailyValue(BigDecimal.valueOf(999)).build();

        BigDecimal gross = calculator.grossAmount(60, false, tariff, pricing);

        assertThat(gross).isEqualByComparingTo(BigDecimal.valueOf(10));
    }
}
