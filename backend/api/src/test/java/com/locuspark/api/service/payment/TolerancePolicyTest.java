package com.locuspark.api.service.payment;

import com.locuspark.api.entity.TariffConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes de TolerancePolicy")
class TolerancePolicyTest {

    private final TolerancePolicy policy = new TolerancePolicy();

    @Test
    @DisplayName("Deve considerar dentro da tolerancia quando os minutos sao iguais ao limite")
    void withinToleranceWhenEqualToLimit() {
        TariffConfiguration tariff = TariffConfiguration.builder().toleranceMinutes(15).build();

        assertThat(policy.isWithinTolerance(15, tariff)).isTrue();
    }

    @Test
    @DisplayName("Deve considerar fora da tolerancia quando os minutos excedem o limite")
    void outsideToleranceWhenAboveLimit() {
        TariffConfiguration tariff = TariffConfiguration.builder().toleranceMinutes(15).build();

        assertThat(policy.isWithinTolerance(16, tariff)).isFalse();
    }

    @Test
    @DisplayName("Deve tratar tolerancia nula como zero minutos")
    void treatsNullToleranceAsZero() {
        TariffConfiguration tariff = TariffConfiguration.builder().toleranceMinutes(null).build();

        assertThat(policy.isWithinTolerance(0, tariff)).isTrue();
        assertThat(policy.isWithinTolerance(1, tariff)).isFalse();
    }
}
