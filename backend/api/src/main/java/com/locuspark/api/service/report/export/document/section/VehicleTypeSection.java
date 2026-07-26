package com.locuspark.api.service.report.export.document.section;

import com.locuspark.api.dto.response.ReportResponse;
import com.locuspark.api.dto.response.report.VehicleTypeSummaryResponse;
import com.locuspark.api.service.report.export.document.ReportCellType;
import com.locuspark.api.service.report.export.document.ReportColumn;
import com.locuspark.api.service.report.export.document.ReportSectionFactory;
import com.locuspark.api.service.report.export.document.ReportTable;
import com.locuspark.api.service.report.export.format.VehicleTypeLabel;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(20)
public class VehicleTypeSection implements ReportSectionFactory {

    @Override
    public ReportTable<VehicleTypeSummaryResponse> build(ReportResponse report) {
        List<ReportColumn<VehicleTypeSummaryResponse>> columns = List.of(
                ReportColumn.of("Tipo de Veículo", ReportCellType.TEXT, r -> VehicleTypeLabel.label(r.type())),
                ReportColumn.of("Quantidade", ReportCellType.INTEGER, VehicleTypeSummaryResponse::ticketCount),
                ReportColumn.of("Receita (R$)", ReportCellType.CURRENCY, VehicleTypeSummaryResponse::revenue),
                ReportColumn.of("Participação", ReportCellType.PERCENT, VehicleTypeSummaryResponse::sharePercent));
        return new ReportTable<>("Tipos de Veículo", columns, report.vehicleTypeSummaries());
    }
}
