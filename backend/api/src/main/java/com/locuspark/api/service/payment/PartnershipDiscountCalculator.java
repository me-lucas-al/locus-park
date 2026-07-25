package com.locuspark.api.service.payment;

import com.locuspark.api.entity.Partnership;
import com.locuspark.api.entity.TariffConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class PartnershipDiscountCalculator {

    private final HourlyRateCalculator hourlyRateCalculator;

    public BigDecimal netAmount(BigDecimal gross, Partnership partnership, long stayMinutes, TariffConfiguration tariff) {
        var type = partnership.getDiscountType();
        BigDecimal discountValue = partnership.getValue();

        if (type == null || discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            return gross;
        }

        return switch (type) {
            case PERCENTAGE -> applyPercentage(gross, discountValue);
            case FIXED_VALUE -> gross.subtract(discountValue);
            case FREE_HOURS -> applyFreeHours(discountValue, stayMinutes, tariff);
        };
    }

    private BigDecimal applyPercentage(BigDecimal gross, BigDecimal discountValue) {
        BigDecimal discountFactor = discountValue.divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
        return gross.subtract(gross.multiply(discountFactor));
    }

    private BigDecimal applyFreeHours(BigDecimal discountValue, long stayMinutes, TariffConfiguration tariff) {
        long freeMinutes = discountValue.longValue() * 60;
        if (stayMinutes <= freeMinutes) {
            return BigDecimal.ZERO;
        }

        long billableMinutes = stayMinutes - freeMinutes;
        long billableHours = (long) Math.ceil(billableMinutes / 60.0);
        return hourlyRateCalculator.amountFor(billableHours, tariff);
    }
}
