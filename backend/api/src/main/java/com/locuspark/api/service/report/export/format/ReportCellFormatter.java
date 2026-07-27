package com.locuspark.api.service.report.export.format;

import com.locuspark.api.service.report.export.document.ReportCellType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class ReportCellFormatter {

    private ReportCellFormatter() {
    }

    public static String format(ReportCellType type, Object value) {
        if (value == null) {
            return "—";
        }
        return switch (type) {
            case TEXT -> value.toString();
            case INTEGER -> NumberFormatter.formatInteger((Number) value);
            case DECIMAL -> NumberFormatter.formatDecimal((Number) value);
            case CURRENCY -> CurrencyFormatter.format((BigDecimal) value);
            case PERCENT -> PercentFormatter.format((Number) value);
            case DATE -> ReportDateFormatter.formatDate((LocalDate) value);
            case DATE_TIME -> ReportDateFormatter.formatDateTime((LocalDateTime) value);
            case DURATION_MINUTES -> DurationFormatter.format(((Number) value).longValue());
        };
    }
}
