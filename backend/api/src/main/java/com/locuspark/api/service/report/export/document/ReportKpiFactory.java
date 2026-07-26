package com.locuspark.api.service.report.export.document;

import com.locuspark.api.dto.response.ReportResponse;
import com.locuspark.api.dto.response.report.OccupancySummaryResponse;
import com.locuspark.api.dto.response.report.RevenueSummaryResponse;
import com.locuspark.api.dto.response.report.StaySummaryResponse;
import com.locuspark.api.service.report.export.format.CurrencyFormatter;
import com.locuspark.api.service.report.export.format.DurationFormatter;
import com.locuspark.api.service.report.export.format.NumberFormatter;
import com.locuspark.api.service.report.export.format.PercentFormatter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReportKpiFactory {

    public List<ReportKpi> buildAll(ReportResponse report) {
        RevenueSummaryResponse revenue = report.summary().revenue();
        StaySummaryResponse stay = report.summary().stay();
        OccupancySummaryResponse occupancy = report.summary().occupancy();

        return List.of(
                new ReportKpi("Faturamento Líquido", CurrencyFormatter.format(revenue.netRevenue())),
                new ReportKpi("Faturamento Bruto", CurrencyFormatter.format(revenue.grossRevenue())),
                new ReportKpi("Desconto Concedido", CurrencyFormatter.format(revenue.discountGranted())),
                new ReportKpi("Ticket Médio", CurrencyFormatter.format(revenue.averageTicketValue())),
                new ReportKpi("Maior Ticket", CurrencyFormatter.format(revenue.highestTicketValue())),
                new ReportKpi("Permanência Média", DurationFormatter.format((long) stay.averageMinutes())),
                new ReportKpi("Permanência Mínima", DurationFormatter.format(stay.minimumMinutes())),
                new ReportKpi("Permanência Máxima", DurationFormatter.format(stay.maximumMinutes())),
                new ReportKpi("Vagas Totais", NumberFormatter.formatInteger(occupancy.totalSpots())),
                new ReportKpi("Entradas", NumberFormatter.formatInteger(occupancy.entryCount())),
                new ReportKpi("Saídas", NumberFormatter.formatInteger(occupancy.exitCount())),
                new ReportKpi("Pico de Ocupação", NumberFormatter.formatInteger(occupancy.peakConcurrentVehicles())),
                new ReportKpi("Taxa Média de Ocupação", PercentFormatter.format(occupancy.averageOccupancyRate() * 100)),
                new ReportKpi("Giro por Vaga", NumberFormatter.formatDecimal(occupancy.turnoverPerSpot())));
    }
}
