package com.locuspark.api.service.report;

import com.locuspark.api.dto.response.report.StaySummaryResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.OptionalLong;

@Component
public class StaySummaryCalculator {

    public StaySummaryResponse calculate(TicketWindow window) {
        List<TicketRecord> all = window.all();

        List<Long> minutes = all.stream()
                .map(TicketRecord::stayMinutes)
                .filter(OptionalLong::isPresent)
                .map(OptionalLong::getAsLong)
                .toList();

        long openStayCount = all.size() - minutes.size();
        long totalMinutes = minutes.stream().mapToLong(Long::longValue).sum();
        double averageMinutes = minutes.isEmpty() ? 0.0 : (double) totalMinutes / minutes.size();
        long minimumMinutes = minutes.stream().mapToLong(Long::longValue).min().orElse(0L);
        long maximumMinutes = minutes.stream().mapToLong(Long::longValue).max().orElse(0L);

        return new StaySummaryResponse(averageMinutes, minimumMinutes, maximumMinutes, totalMinutes, openStayCount);
    }
}
