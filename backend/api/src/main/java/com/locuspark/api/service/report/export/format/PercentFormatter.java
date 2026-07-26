package com.locuspark.api.service.report.export.format;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public final class PercentFormatter {

    private PercentFormatter() {
    }

    // valor na escala 0-100 (28.0 = 28%), não 0-1
    public static String format(Number value) {
        double safe = value != null ? value.doubleValue() : 0.0;
        DecimalFormat format = new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(ReportLocale.PT_BR));
        return format.format(safe) + "%";
    }
}
