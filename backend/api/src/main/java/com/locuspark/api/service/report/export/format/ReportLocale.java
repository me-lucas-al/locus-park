package com.locuspark.api.service.report.export.format;

import java.time.ZoneId;
import java.util.Locale;

public final class ReportLocale {

    public static final Locale PT_BR = Locale.of("pt", "BR");
    public static final ZoneId TIME_ZONE = ZoneId.of("America/Sao_Paulo");
    public static final String TIME_ZONE_LABEL = "America/Sao_Paulo (UTC-3)";

    private ReportLocale() {
    }
}
