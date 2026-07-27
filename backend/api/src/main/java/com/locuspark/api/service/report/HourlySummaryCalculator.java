package com.locuspark.api.service.report;

import com.locuspark.api.dto.response.report.HourlySummaryResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class HourlySummaryCalculator {

    public List<HourlySummaryResponse> calculate(TicketWindow window) {
        long[] entryCounts = new long[24];
        long[] exitCounts = new long[24];
        BigDecimal[] revenue = new BigDecimal[24];
        for (int hour = 0; hour < 24; hour++) {
            revenue[hour] = BigDecimal.ZERO;
        }

        for (TicketRecord record : window.entered()) {
            entryCounts[record.enteredAtLocal().getHour()]++;
        }

        for (TicketRecord record : window.paid()) {
            int hour = record.exitedAtLocal().getHour();
            exitCounts[hour]++;
            revenue[hour] = revenue[hour].add(record.net());
        }

        return java.util.stream.IntStream.range(0, 24)
                .mapToObj(hour -> new HourlySummaryResponse(hour, entryCounts[hour], exitCounts[hour], revenue[hour]))
                .toList();
    }
}
