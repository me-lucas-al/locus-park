package com.locuspark.api.service.payment;

import com.locuspark.api.entity.TariffConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes de HourlyRateCalculator")
class HourlyRateCalculatorTest {

    private final HourlyRateCalculator calculator = new HourlyRateCalculator();

    @Test
    @DisplayName("Deve cobrar apenas a primeira hora quando billableHours <= 1")
    void chargesOnlyFirstHour() {
        TariffConfiguration tariff = TariffConfiguration.builder()
                .firstHourValue(BigDecimal.valueOf(10))
                .additionalFractionValue(BigDecimal.valueOf(2))
                .build();

        assertThat(calculator.amountFor(1, tariff)).isEqualByComparingTo(BigDecimal.valueOf(10));
    }

    @Test
    @DisplayName("Deve somar fracoes adicionais para horas acima de 1")
    void addsAdditionalFractionsForMoreThanOneHour() {
        TariffConfiguration tariff = TariffConfiguration.builder()
                .firstHourValue(BigDecimal.valueOf(10))
                .additionalFractionValue(BigDecimal.valueOf(2))
                .build();

        assertThat(calculator.amountFor(4, tariff)).isEqualByComparingTo(BigDecimal.valueOf(16));
    }

    @Test
    @DisplayName("Deve tratar valores nulos de tarifa como zero")
    void treatsNullTariffValuesAsZero() {
        TariffConfiguration tariff = TariffConfiguration.builder().build();

        assertThat(calculator.amountFor(3, tariff)).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
