package com.locuspark.api.service.report.export.format;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public final class NumberFormatter {

    private NumberFormatter() {
    }

    public static String formatInteger(Number value) {
        long safe = value != null ? value.longValue() : 0L;
        DecimalFormat format = new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(ReportLocale.PT_BR));
        return format.format(safe);
    }

    public static String formatDecimal(Number value) {
        double safe = value != null ? value.doubleValue() : 0.0;
        DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(ReportLocale.PT_BR));
        return format.format(safe);
    }
}
