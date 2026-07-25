package com.locuspark.api.service.report;

import com.locuspark.api.dto.response.report.RevenueSummaryResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class RevenueSummaryCalculator {

    public RevenueSummaryResponse calculate(TicketWindow window) {
        List<TicketRecord> paid = window.paid();

        BigDecimal gross = paid.stream().map(TicketRecord::gross).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = paid.stream().map(TicketRecord::discount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal net = paid.stream().map(TicketRecord::net).reduce(BigDecimal.ZERO, BigDecimal::add);

        long paidCount = paid.size();
        long freeExitCount = paid.stream().filter(r -> r.net().compareTo(BigDecimal.ZERO) == 0).count();

        BigDecimal average = paidCount > 0
                ? net.divide(BigDecimal.valueOf(paidCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal highest = paid.stream().map(TicketRecord::net).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal lowest = paid.stream().map(TicketRecord::net).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        return new RevenueSummaryResponse(gross, discount, net, average, highest, lowest, paidCount, freeExitCount);
    }
}
