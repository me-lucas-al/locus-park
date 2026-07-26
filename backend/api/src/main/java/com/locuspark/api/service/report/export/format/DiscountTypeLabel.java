package com.locuspark.api.service.report.export.format;

import com.locuspark.api.enums.DiscountType;

public final class DiscountTypeLabel {

    private DiscountTypeLabel() {
    }

    public static String label(DiscountType type) {
        if (type == null) {
            return "—";
        }
        return switch (type) {
            case PERCENTAGE -> "Percentual";
            case FIXED_VALUE -> "Valor Fixo";
            case FREE_HOURS -> "Horas Grátis";
        };
    }
}
