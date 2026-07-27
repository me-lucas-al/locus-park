package com.locuspark.api.service.report;

import com.locuspark.api.dto.response.report.TicketRowResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.OptionalLong;

@Component
public class TicketRowMapper {

    public List<TicketRowResponse> map(List<TicketRecord> records, ReportDetailLimit limit) {
        return records.stream().limit(limit.maxRows()).map(this::toRow).toList();
    }

    private TicketRowResponse toRow(TicketRecord record) {
        OptionalLong stayMinutes = record.stayMinutes();
        return new TicketRowResponse(
                record.ticketId(),
                record.status(),
                record.plate().getValue(),
                record.model(),
                record.color(),
                record.vehicleType(),
                record.clientName(),
                record.clientCpf() != null ? record.clientCpf().getValue() : null,
                record.enteredAtLocal(),
                record.exitedAtLocal(),
                stayMinutes.isPresent() ? stayMinutes.getAsLong() : null,
                record.partnershipName(),
                record.paymentMethod(),
                record.grossAmount(),
                record.discountAmount(),
                record.totalAmount());
    }
}
