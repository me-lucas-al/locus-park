package com.locuspark.api.service.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record StayCharge(BigDecimal gross, BigDecimal discount, BigDecimal net) {

    public static StayCharge free() {
        BigDecimal zero = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return new StayCharge(zero, zero, zero);
    }

    public static StayCharge of(BigDecimal gross, BigDecimal net) {
        BigDecimal scaledGross = gross.setScale(2, RoundingMode.HALF_UP);
        BigDecimal scaledNet = net.setScale(2, RoundingMode.HALF_UP);

        if (scaledNet.compareTo(scaledGross) > 0) {
            scaledNet = scaledGross;
        }
        if (scaledNet.signum() < 0) {
            scaledNet = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal discount = scaledGross.subtract(scaledNet);
        if (discount.signum() < 0) {
            discount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return new StayCharge(scaledGross, discount, scaledNet);
    }
}
