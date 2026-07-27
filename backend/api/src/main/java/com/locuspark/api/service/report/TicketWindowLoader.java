package com.locuspark.api.service.report;

import com.locuspark.api.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TicketWindowLoader {

    private final TicketRepository ticketRepository;

    public TicketWindow load(UUID companyId, Instant fromInclusive, Instant toExclusive) {
        var paid = ticketRepository.findPaidRecordsByExitWindow(companyId, fromInclusive, toExclusive);
        var entered = ticketRepository.findRecordsByEntryWindow(companyId, fromInclusive, toExclusive);
        long presentAtStart = ticketRepository.countPresentAt(companyId, fromInclusive);
        return TicketWindow.of(paid, entered, presentAtStart);
    }
}
