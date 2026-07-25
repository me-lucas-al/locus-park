package com.locuspark.api.service.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes de StayCharge")
class StayChargeTest {

    @Test
    @DisplayName("free() deve retornar bruto, desconto e liquido zerados")
    void freeReturnsAllZero() {
        StayCharge charge = StayCharge.free();

        assertThat(charge.gross()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(charge.discount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(charge.net()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("of() deve derivar o desconto como bruto menos liquido")
    void ofDerivesDiscountFromGrossAndNet() {
        StayCharge charge = StayCharge.of(BigDecimal.valueOf(100), BigDecimal.valueOf(80));

        assertThat(charge.gross()).isEqualByComparingTo("100.00");
        assertThat(charge.net()).isEqualByComparingTo("80.00");
        assertThat(charge.discount()).isEqualByComparingTo("20.00");
    }

    @Test
    @DisplayName("of() deve garantir gross == net + discount para todos os casos")
    void ofKeepsGrossEqualsNetPlusDiscountInvariant() {
        StayCharge charge = StayCharge.of(BigDecimal.valueOf(37.333), BigDecimal.valueOf(10.111));

        assertThat(charge.gross()).isEqualByComparingTo(charge.net().add(charge.discount()));
    }

    @Test
    @DisplayName("of() deve clampar o liquido no bruto quando o recalculo de FREE_HOURS excede o bruto")
    void ofClampsNetToGrossWhenRecalculationExceedsGross() {
        StayCharge charge = StayCharge.of(BigDecimal.valueOf(50), BigDecimal.valueOf(70));

        assertThat(charge.net()).isEqualByComparingTo("50.00");
        assertThat(charge.discount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(charge.gross()).isEqualByComparingTo(charge.net().add(charge.discount()));
    }

    @Test
    @DisplayName("of() nunca deve produzir desconto negativo")
    void ofNeverProducesNegativeDiscount() {
        StayCharge charge = StayCharge.of(BigDecimal.valueOf(10), BigDecimal.valueOf(-5));

        assertThat(charge.net()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(charge.discount()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }
}
