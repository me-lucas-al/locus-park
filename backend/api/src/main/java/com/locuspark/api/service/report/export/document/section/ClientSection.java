package com.locuspark.api.service.report.export.document.section;

import com.locuspark.api.dto.response.ReportResponse;
import com.locuspark.api.dto.response.report.ClientSummaryResponse;
import com.locuspark.api.service.report.export.document.ReportCellType;
import com.locuspark.api.service.report.export.document.ReportColumn;
import com.locuspark.api.service.report.export.document.ReportSectionFactory;
import com.locuspark.api.service.report.export.document.ReportTable;
import com.locuspark.api.service.report.export.format.DocumentFormatter;
import com.locuspark.api.service.report.export.format.PaymentMethodLabel;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Order(60)
public class ClientSection implements ReportSectionFactory {

    @Override
    public ReportTable<ClientSummaryResponse> build(ReportResponse report) {
        List<ReportColumn<ClientSummaryResponse>> columns = List.of(
                ReportColumn.of("Cliente", ReportCellType.TEXT, ClientSummaryResponse::name),
                ReportColumn.of("CPF", ReportCellType.TEXT, r -> DocumentFormatter.formatCpf(r.cpf())),
                ReportColumn.of("Tickets", ReportCellType.INTEGER, ClientSummaryResponse::ticketCount),
                ReportColumn.of("Total Gasto (R$)", ReportCellType.CURRENCY, ClientSummaryResponse::totalSpent),
                ReportColumn.of("Permanência Média", ReportCellType.DURATION_MINUTES,
                        r -> Math.round(r.averageStayMinutes())),
                ReportColumn.of("Formas de Pagamento", ReportCellType.TEXT, this::paymentMethodsUsed));
        return new ReportTable<>("Clientes", columns, report.clientSummaries());
    }

    private String paymentMethodsUsed(ClientSummaryResponse client) {
        return client.paymentMethodsUsed().stream()
                .map(PaymentMethodLabel::label)
                .collect(Collectors.joining(", "));
    }
}
