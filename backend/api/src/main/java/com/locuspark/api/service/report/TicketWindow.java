package com.locuspark.api.service.report;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record TicketWindow(List<TicketRecord> paid, List<TicketRecord> entered, List<TicketRecord> all, long presentAtStart) {

    public static TicketWindow of(List<TicketRecord> paid, List<TicketRecord> entered, long presentAtStart) {
        Map<java.util.UUID, TicketRecord> deduplicated = new LinkedHashMap<>();
        paid.forEach(record -> deduplicated.put(record.ticketId(), record));
        entered.forEach(record -> deduplicated.put(record.ticketId(), record));

        List<TicketRecord> all = deduplicated.values().stream()
                .sorted(Comparator.comparing(TicketRecord::enteredAt).reversed())
                .toList();

        return new TicketWindow(paid, entered, all, presentAtStart);
    }
}
