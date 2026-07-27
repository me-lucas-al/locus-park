package com.locuspark.api.service.report;

import com.locuspark.api.dto.response.report.DailySummaryResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DailySummaryCalculator {

    private record Bucket(long entryCount, long exitCount, BigDecimal revenue, BigDecimal discount) {
        static Bucket empty() {
            return new Bucket(0, 0, BigDecimal.ZERO, BigDecimal.ZERO);
        }
    }

    public java.util.List<DailySummaryResponse> calculate(TicketWindow window, LocalDate from, LocalDate to) {
        Map<LocalDate, Bucket> buckets = new LinkedHashMap<>();
        for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
            buckets.put(day, Bucket.empty());
        }

        for (TicketRecord record : window.entered()) {
            LocalDate day = record.enteredAtLocal().toLocalDate();
            buckets.computeIfPresent(day, (d, bucket) -> new Bucket(bucket.entryCount() + 1, bucket.exitCount(), bucket.revenue(), bucket.discount()));
        }

        for (TicketRecord record : window.paid()) {
            LocalDate day = record.exitedAtLocal().toLocalDate();
            buckets.computeIfPresent(day, (d, bucket) -> new Bucket(
                    bucket.entryCount(), bucket.exitCount() + 1,
                    bucket.revenue().add(record.net()), bucket.discount().add(record.discount())));
        }

        return buckets.entrySet().stream()
                .map(entry -> new DailySummaryResponse(entry.getKey(), entry.getValue().entryCount(),
                        entry.getValue().exitCount(), entry.getValue().revenue(), entry.getValue().discount()))
                .toList();
    }
}
