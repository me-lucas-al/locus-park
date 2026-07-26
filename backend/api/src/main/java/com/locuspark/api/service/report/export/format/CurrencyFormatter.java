package com.locuspark.api.service.report.export.format;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public final class CurrencyFormatter {

    private CurrencyFormatter() {
    }

    public static String format(BigDecimal value) {
        return "R$ " + formatBare(value);
    }

    public static String formatBare(BigDecimal value) {
        BigDecimal safe = value != null ? value : BigDecimal.ZERO;
        DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(ReportLocale.PT_BR));
        return format.format(safe);
    }
}
