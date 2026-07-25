package com.locuspark.api.service.report;

import com.locuspark.api.dto.response.report.VehicleTypeSummaryResponse;
import com.locuspark.api.enums.VehicleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class VehicleTypeSummaryCalculator {

    private final SharePercentCalculator sharePercentCalculator;

    public List<VehicleTypeSummaryResponse> calculate(TicketWindow window) {
        Map<VehicleType, BigDecimal> revenueByType = new EnumMap<>(VehicleType.class);
        Map<VehicleType, Long> countByType = new EnumMap<>(VehicleType.class);
        for (VehicleType type : VehicleType.values()) {
            revenueByType.put(type, BigDecimal.ZERO);
            countByType.put(type, 0L);
        }

        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (TicketRecord record : window.paid()) {
            revenueByType.merge(record.vehicleType(), record.net(), BigDecimal::add);
            countByType.merge(record.vehicleType(), 1L, Long::sum);
            totalRevenue = totalRevenue.add(record.net());
        }

        BigDecimal finalTotalRevenue = totalRevenue;
        return revenueByType.entrySet().stream()
                .map(entry -> new VehicleTypeSummaryResponse(
                        entry.getKey(),
                        countByType.get(entry.getKey()),
                        entry.getValue(),
                        sharePercentCalculator.shareOf(entry.getValue(), finalTotalRevenue)))
                .toList();
    }
}
