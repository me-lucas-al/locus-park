package com.locuspark.api.service.report.export.document.section;

import com.locuspark.api.dto.response.ReportResponse;
import com.locuspark.api.dto.response.report.PaymentMethodSummaryResponse;
import com.locuspark.api.service.report.export.document.ReportCellType;
import com.locuspark.api.service.report.export.document.ReportColumn;
import com.locuspark.api.service.report.export.document.ReportSectionFactory;
import com.locuspark.api.service.report.export.document.ReportTable;
import com.locuspark.api.service.report.export.format.PaymentMethodLabel;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(10)
public class PaymentMethodSection implements ReportSectionFactory {

    @Override
    public ReportTable<PaymentMethodSummaryResponse> build(ReportResponse report) {
        List<ReportColumn<PaymentMethodSummaryResponse>> columns = List.of(
                ReportColumn.of("Forma de Pagamento", ReportCellType.TEXT, r -> PaymentMethodLabel.label(r.method())),
                ReportColumn.of("Quantidade", ReportCellType.INTEGER, PaymentMethodSummaryResponse::ticketCount),
                ReportColumn.of("Receita (R$)", ReportCellType.CURRENCY, PaymentMethodSummaryResponse::revenue),
                ReportColumn.of("Participação", ReportCellType.PERCENT, PaymentMethodSummaryResponse::sharePercent));
        return new ReportTable<>("Formas de Pagamento", columns, report.paymentMethodSummaries());
    }
}
