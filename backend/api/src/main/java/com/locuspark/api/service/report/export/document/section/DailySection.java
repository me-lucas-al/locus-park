package com.locuspark.api.service.report.export.document.section;

import com.locuspark.api.dto.response.ReportResponse;
import com.locuspark.api.dto.response.report.DailySummaryResponse;
import com.locuspark.api.service.report.export.document.ReportCellType;
import com.locuspark.api.service.report.export.document.ReportColumn;
import com.locuspark.api.service.report.export.document.ReportSectionFactory;
import com.locuspark.api.service.report.export.document.ReportTable;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(30)
public class DailySection implements ReportSectionFactory {

    @Override
    public ReportTable<DailySummaryResponse> build(ReportResponse report) {
        List<ReportColumn<DailySummaryResponse>> columns = List.of(
                ReportColumn.of("Data", ReportCellType.DATE, DailySummaryResponse::date),
                ReportColumn.of("Entradas (entrada)", ReportCellType.INTEGER, DailySummaryResponse::entryCount),
                ReportColumn.of("Saídas (saída)", ReportCellType.INTEGER, DailySummaryResponse::exitCount),
                ReportColumn.of("Faturamento (saída)", ReportCellType.CURRENCY, DailySummaryResponse::revenue),
                ReportColumn.of("Desconto (saída)", ReportCellType.CURRENCY, DailySummaryResponse::discount));
        return new ReportTable<>("Movimentação Diária", columns, report.dailySummaries());
    }
}
