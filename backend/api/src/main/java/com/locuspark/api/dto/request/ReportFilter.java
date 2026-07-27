package com.locuspark.api.dto.request;

import com.locuspark.api.exception.BusinessException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public record ReportFilter(LocalDate from, LocalDate to) {

    public ReportFilter {
        to = to != null ? to : LocalDate.now();
        from = from != null ? from : to.minusDays(29);
        if (from.isAfter(to)) {
            throw new BusinessException("A data inicial não pode ser posterior à data final.");
        }
        if (ChronoUnit.DAYS.between(from, to) >= 366) {
            throw new BusinessException("O período do relatório não pode exceder 366 dias.");
        }
    }

    private static final ZoneId PATIO_ZONE = ZoneId.of("America/Sao_Paulo");

    public LocalDateTime fromInclusive() {
        return from.atStartOfDay();
    }

    public LocalDateTime toExclusive() {
        return to.plusDays(1).atStartOfDay();
    }

    public Instant fromInstant() {
        return from.atStartOfDay(PATIO_ZONE).toInstant();
    }

    public Instant toInstant() {
        return to.plusDays(1).atStartOfDay(PATIO_ZONE).toInstant();
    }

    public long days() {
        return ChronoUnit.DAYS.between(from, to) + 1;
    }
}
