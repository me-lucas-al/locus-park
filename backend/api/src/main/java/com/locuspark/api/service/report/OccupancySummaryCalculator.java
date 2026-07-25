package com.locuspark.api.service.report;

import com.locuspark.api.dto.response.report.OccupancySummaryResponse;
import com.locuspark.api.enums.TicketStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class OccupancySummaryCalculator {

    private record Event(LocalDateTime at, int delta) {
    }

    public OccupancySummaryResponse calculate(TicketWindow window, Integer totalSpots, LocalDateTime fromInclusive, LocalDateTime toExclusive) {
        List<Event> events = new ArrayList<>();
        for (TicketRecord record : window.all()) {
            if (!record.enteredAt().isBefore(fromInclusive)) {
                events.add(new Event(record.enteredAt(), 1));
            }
            if (record.exitedAt() != null && !record.exitedAt().isBefore(fromInclusive) && record.exitedAt().isBefore(toExclusive)) {
                events.add(new Event(record.exitedAt(), -1));
            }
        }
        events.sort(Comparator.comparing(Event::at).thenComparingInt(Event::delta));

        long counter = window.presentAtStart();
        long peak = counter;
        LocalDateTime peakAt = fromInclusive;
        long weightedMinutes = 0;
        LocalDateTime cursor = fromInclusive;

        for (Event event : events) {
            weightedMinutes += counter * Duration.between(cursor, event.at()).toMinutes();
            cursor = event.at();
            counter = Math.max(0, counter + event.delta());
            if (counter > peak) {
                peak = counter;
                peakAt = event.at();
            }
        }
        weightedMinutes += counter * Duration.between(cursor, toExclusive).toMinutes();

        long windowMinutes = Duration.between(fromInclusive, toExclusive).toMinutes();
        double averageOccupancy = windowMinutes > 0 ? (double) weightedMinutes / windowMinutes : 0.0;

        int spots = totalSpots != null ? totalSpots : 0;
        long entryCount = window.entered().size();
        long exitCount = window.paid().size();
        long activeCount = window.all().stream().filter(record -> record.status() == TicketStatus.ACTIVE).count();

        double peakRate = spots > 0 ? (double) peak / spots : 0.0;
        double averageRate = spots > 0 ? averageOccupancy / spots : 0.0;
        double turnoverPerSpot = spots > 0 ? (double) exitCount / spots : 0.0;

        return new OccupancySummaryResponse(spots, entryCount, exitCount, activeCount, peak, peakAt, peakRate, averageRate, turnoverPerSpot);
    }
}
