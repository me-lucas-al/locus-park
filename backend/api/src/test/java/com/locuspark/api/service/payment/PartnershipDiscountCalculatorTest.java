package com.locuspark.api.service.payment;

import com.locuspark.api.entity.Partnership;
import com.locuspark.api.entity.TariffConfiguration;
import com.locuspark.api.enums.DiscountType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes de PartnershipDiscountCalculator")
class PartnershipDiscountCalculatorTest {

    private PartnershipDiscountCalculator calculator;
    private TariffConfiguration tariff;

    @BeforeEach
    void setUp() {
        calculator = new PartnershipDiscountCalculator(new HourlyRateCalculator());
        tariff = TariffConfiguration.builder()
                .firstHourValue(BigDecimal.valueOf(10))
                .additionalFractionValue(BigDecimal.valueOf(2))
                .build();
    }

    @Test
    @DisplayName("Deve aplicar desconto percentual sobre o bruto")
    void appliesPercentageDiscount() {
        Partnership partnership = Partnership.builder().discountType(DiscountType.PERCENTAGE).value(BigDecimal.valueOf(10)).build();

        BigDecimal net = calculator.netAmount(BigDecimal.valueOf(100), partnership, 180, tariff);

        assertThat(net).isEqualByComparingTo(BigDecimal.valueOf(90));
    }

    @Test
    @DisplayName("Deve aplicar desconto de valor fixo sobre o bruto")
    void appliesFixedValueDiscount() {
        Partnership partnership = Partnership.builder().discountType(DiscountType.FIXED_VALUE).value(BigDecimal.valueOf(15)).build();

        BigDecimal net = calculator.netAmount(BigDecimal.valueOf(100), partnership, 180, tariff);

        assertThat(net).isEqualByComparingTo(BigDecimal.valueOf(85));
    }

    @Test
    @DisplayName("FREE_HOURS deve zerar quando a estadia inteira esta dentro das horas livres")
    void freeHoursReturnsZeroWhenEntireStayIsCovered() {
        Partnership partnership = Partnership.builder().discountType(DiscountType.FREE_HOURS).value(BigDecimal.valueOf(2)).build();

        BigDecimal net = calculator.netAmount(BigDecimal.valueOf(100), partnership, 90, tariff);

        assertThat(net).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("FREE_HOURS deve recalcular a partir do restante cobravel, nao subtrair do bruto")
    void freeHoursRecalculatesFromRemainingBillableTime() {
        Partnership partnership = Partnership.builder().discountType(DiscountType.FREE_HOURS).value(BigDecimal.valueOf(1)).build();

        BigDecimal net = calculator.netAmount(BigDecimal.valueOf(500), partnership, 181, tariff);

        assertThat(net).isEqualByComparingTo(BigDecimal.valueOf(14));
    }

    @Test
    @DisplayName("Deve ignorar desconto quando tipo ou valor sao nulos ou nao positivos")
    void ignoresDiscountWhenTypeOrValueMissing() {
        Partnership partnership = Partnership.builder().discountType(null).value(null).build();

        BigDecimal net = calculator.netAmount(BigDecimal.valueOf(100), partnership, 180, tariff);

        assertThat(net).isEqualByComparingTo(BigDecimal.valueOf(100));
    }
}
