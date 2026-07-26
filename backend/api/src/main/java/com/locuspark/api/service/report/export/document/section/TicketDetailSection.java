package com.locuspark.api.service.report.export.document.section;

import com.locuspark.api.dto.response.ReportResponse;
import com.locuspark.api.dto.response.report.TicketRowResponse;
import com.locuspark.api.service.report.export.document.ReportCellType;
import com.locuspark.api.service.report.export.document.ReportColumn;
import com.locuspark.api.service.report.export.document.ReportSectionFactory;
import com.locuspark.api.service.report.export.document.ReportTable;
import com.locuspark.api.service.report.export.format.PaymentMethodLabel;
import com.locuspark.api.service.report.export.format.PlateFormatter;
import com.locuspark.api.service.report.export.format.TicketStatusLabel;
import com.locuspark.api.service.report.export.format.VehicleTypeLabel;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(70)
public class TicketDetailSection implements ReportSectionFactory {

    @Override
    public ReportTable<TicketRowResponse> build(ReportResponse report) {
        List<ReportColumn<TicketRowResponse>> columns = List.of(
                ReportColumn.dataOnly("ID", ReportCellType.TEXT, r -> r.ticketId().toString()),
                ReportColumn.dataOnly("Status", ReportCellType.TEXT, r -> TicketStatusLabel.label(r.status())),
                ReportColumn.of("Placa", ReportCellType.TEXT, r -> PlateFormatter.format(r.plate())),
                ReportColumn.dataOnly("Modelo", ReportCellType.TEXT, TicketRowResponse::model),
                ReportColumn.dataOnly("Cor", ReportCellType.TEXT, TicketRowResponse::color),
                ReportColumn.of("Tipo", ReportCellType.TEXT, r -> VehicleTypeLabel.label(r.vehicleType())),
                ReportColumn.of("Cliente", ReportCellType.TEXT, r -> r.clientName() != null ? r.clientName() : "—"),
                ReportColumn.dataOnly("CPF", ReportCellType.TEXT, TicketRowResponse::clientCpf),
                ReportColumn.of("Entrada", ReportCellType.DATE_TIME, TicketRowResponse::enteredAt),
                ReportColumn.of("Saída", ReportCellType.DATE_TIME, TicketRowResponse::exitedAt),
                ReportColumn.of("Permanência", ReportCellType.DURATION_MINUTES, TicketRowResponse::stayMinutes),
                ReportColumn.of("Convênio", ReportCellType.TEXT, r -> r.partnershipName() != null ? r.partnershipName() : "—"),
                ReportColumn.of("Pagamento", ReportCellType.TEXT, r -> PaymentMethodLabel.label(r.paymentMethod())),
                ReportColumn.of("Bruto", ReportCellType.CURRENCY, TicketRowResponse::grossAmount),
                ReportColumn.of("Desconto", ReportCellType.CURRENCY, TicketRowResponse::discountAmount),
                ReportColumn.of("Total", ReportCellType.CURRENCY, TicketRowResponse::totalAmount));
        return new ReportTable<>("Detalhamento de Tickets", columns, report.tickets());
    }
}
