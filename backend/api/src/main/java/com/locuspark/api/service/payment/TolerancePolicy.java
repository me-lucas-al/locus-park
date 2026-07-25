package com.locuspark.api.service.payment;

import com.locuspark.api.entity.TariffConfiguration;
import org.springframework.stereotype.Component;

@Component
public class TolerancePolicy {

    public boolean isWithinTolerance(long stayMinutes, TariffConfiguration tariff) {
        int tolerance = tariff.getToleranceMinutes() != null ? tariff.getToleranceMinutes() : 0;
        return stayMinutes <= tolerance;
    }
}
