package com.locuspark.api.service.report.export.document.section;

import com.locuspark.api.dto.response.ReportResponse;
import com.locuspark.api.dto.response.report.PartnershipSummaryResponse;
import com.locuspark.api.service.report.export.document.ReportCellType;
import com.locuspark.api.service.report.export.document.ReportColumn;
import com.locuspark.api.service.report.export.document.ReportSectionFactory;
import com.locuspark.api.service.report.export.document.ReportTable;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(50)
public class PartnershipSection implements ReportSectionFactory {

    @Override
    public ReportTable<PartnershipSummaryResponse> build(ReportResponse report) {
        List<ReportColumn<PartnershipSummaryResponse>> columns = List.of(
                ReportColumn.of("Convênio", ReportCellType.TEXT, PartnershipSummaryResponse::name),
                ReportColumn.of("Utilizações", ReportCellType.INTEGER, PartnershipSummaryResponse::usageCount),
                ReportColumn.of("Desconto Concedido (R$)", ReportCellType.CURRENCY, PartnershipSummaryResponse::discountGranted));
        return new ReportTable<>("Convênios", columns, report.partnershipSummaries());
    }
}
