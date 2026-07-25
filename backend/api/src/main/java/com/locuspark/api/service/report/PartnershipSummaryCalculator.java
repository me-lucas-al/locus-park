package com.locuspark.api.service.report;

import com.locuspark.api.dto.response.report.PartnershipSummaryResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class PartnershipSummaryCalculator {

    private record Accumulator(String name, long usageCount, BigDecimal discountGranted) {
    }

    public List<PartnershipSummaryResponse> calculate(TicketWindow window) {
        Map<UUID, Accumulator> accumulators = new LinkedHashMap<>();

        for (TicketRecord record : window.all()) {
            if (record.partnershipId() == null) {
                continue;
            }
            accumulators.merge(record.partnershipId(), new Accumulator(record.partnershipName(), 1, BigDecimal.ZERO),
                    (oldValue, newValue) -> new Accumulator(oldValue.name(), oldValue.usageCount() + 1, oldValue.discountGranted()));
        }

        for (TicketRecord record : window.paid()) {
            if (record.partnershipId() == null) {
                continue;
            }
            accumulators.computeIfPresent(record.partnershipId(), (id, acc) ->
                    new Accumulator(acc.name(), acc.usageCount(), acc.discountGranted().add(record.discount())));
        }

        return accumulators.entrySet().stream()
                .map(entry -> new PartnershipSummaryResponse(entry.getKey(), entry.getValue().name(),
                        entry.getValue().usageCount(), entry.getValue().discountGranted()))
                .toList();
    }
}
