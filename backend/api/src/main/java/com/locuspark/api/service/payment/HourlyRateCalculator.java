package com.locuspark.api.service.payment;

import com.locuspark.api.entity.TariffConfiguration;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class HourlyRateCalculator {

    public BigDecimal amountFor(long billableHours, TariffConfiguration tariff) {
        BigDecimal firstHourValue = tariff.getFirstHourValue() != null ? tariff.getFirstHourValue() : BigDecimal.ZERO;
        BigDecimal additionalFractionValue = tariff.getAdditionalFractionValue() != null
                ? tariff.getAdditionalFractionValue() : BigDecimal.ZERO;

        if (billableHours <= 1) {
            return firstHourValue;
        }

        long additionalHours = billableHours - 1;
        return firstHourValue.add(additionalFractionValue.multiply(BigDecimal.valueOf(additionalHours)));
    }
}
