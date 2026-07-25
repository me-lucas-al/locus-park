package com.locuspark.api.service.report;

import com.locuspark.api.dto.response.report.PaymentMethodSummaryResponse;
import com.locuspark.api.enums.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentMethodSummaryCalculator {

    private final SharePercentCalculator sharePercentCalculator;

    public List<PaymentMethodSummaryResponse> calculate(TicketWindow window) {
        Map<PaymentMethod, BigDecimal> revenueByMethod = new EnumMap<>(PaymentMethod.class);
        Map<PaymentMethod, Long> countByMethod = new EnumMap<>(PaymentMethod.class);
        for (PaymentMethod method : PaymentMethod.values()) {
            revenueByMethod.put(method, BigDecimal.ZERO);
            countByMethod.put(method, 0L);
        }

        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (TicketRecord record : window.paid()) {
            if (record.paymentMethod() == null) {
                continue;
            }
            revenueByMethod.merge(record.paymentMethod(), record.net(), BigDecimal::add);
            countByMethod.merge(record.paymentMethod(), 1L, Long::sum);
            totalRevenue = totalRevenue.add(record.net());
        }

        BigDecimal finalTotalRevenue = totalRevenue;
        return revenueByMethod.entrySet().stream()
                .map(entry -> new PaymentMethodSummaryResponse(
                        entry.getKey(),
                        countByMethod.get(entry.getKey()),
                        entry.getValue(),
                        sharePercentCalculator.shareOf(entry.getValue(), finalTotalRevenue)))
                .toList();
    }
}
