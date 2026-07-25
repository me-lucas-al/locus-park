package com.locuspark.api.service.payment;

import com.locuspark.api.entity.PricingConfiguration;
import com.locuspark.api.entity.TariffConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class GrossStayChargeCalculator {

    private final HourlyRateCalculator hourlyRateCalculator;

    public BigDecimal grossAmount(long stayMinutes, boolean crossedDate, TariffConfiguration tariff, PricingConfiguration pricing) {
        long hoursToCharge = (long) Math.ceil(stayMinutes / 60.0);
        int dailyTrigger = pricing.getDailyTriggerHours() != null ? pricing.getDailyTriggerHours() : 24;

        BigDecimal baseAmount = hoursToCharge >= dailyTrigger
                ? (pricing.getDailyValue() != null ? pricing.getDailyValue() : BigDecimal.ZERO)
                : hourlyRateCalculator.amountFor(hoursToCharge, tariff);

        if (crossedDate && tariff.getOvernightFee() != null) {
            baseAmount = baseAmount.add(tariff.getOvernightFee());
        }

        return baseAmount;
    }
}
