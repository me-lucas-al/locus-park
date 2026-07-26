package com.locuspark.api.service.report.export.format;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ReportDateFormatter {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", ReportLocale.PT_BR);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", ReportLocale.PT_BR);

    private ReportDateFormatter() {
    }

    public static String formatDate(LocalDate value) {
        return value != null ? value.format(DATE) : "—";
    }

    public static String formatDateTime(LocalDateTime value) {
        return value != null ? value.format(DATE_TIME) : "—";
    }
}
