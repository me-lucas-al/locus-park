package com.locuspark.api.service.report;

import com.locuspark.api.dto.response.report.ClientSummaryResponse;
import com.locuspark.api.enums.TicketStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ClientSummaryCalculator {

    public List<ClientSummaryResponse> calculate(TicketWindow window) {
        Map<UUID, List<TicketRecord>> byClient = window.all().stream()
                .filter(record -> record.clientId() != null)
                .collect(Collectors.groupingBy(TicketRecord::clientId, LinkedHashMap::new, Collectors.toList()));

        return byClient.entrySet().stream()
                .map(entry -> toSummary(entry.getKey(), entry.getValue()))
                .toList();
    }

    private ClientSummaryResponse toSummary(UUID clientId, List<TicketRecord> records) {
        TicketRecord any = records.get(0);

        BigDecimal totalSpent = records.stream()
                .filter(record -> record.status() == TicketStatus.PAID)
                .map(TicketRecord::net)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Long> minutes = records.stream()
                .map(TicketRecord::stayMinutes)
                .filter(OptionalLong::isPresent)
                .map(OptionalLong::getAsLong)
                .toList();
        double averageStayMinutes = minutes.isEmpty() ? 0.0 : minutes.stream().mapToLong(Long::longValue).average().orElse(0.0);

        var paymentMethodsUsed = records.stream()
                .filter(record -> record.status() == TicketStatus.PAID && record.paymentMethod() != null)
                .map(TicketRecord::paymentMethod)
                .distinct()
                .sorted(Comparator.comparingInt(Enum::ordinal))
                .toList();

        String cpf = any.clientCpf() != null ? any.clientCpf().getValue() : null;
        return new ClientSummaryResponse(clientId, any.clientName(), cpf, records.size(), totalSpent, averageStayMinutes, paymentMethodsUsed);
    }
}
