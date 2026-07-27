package com.locuspark.api.service.report.export.csv;

import com.locuspark.api.service.report.export.document.ReportCellType;
import com.locuspark.api.service.report.export.document.ReportTable;
import com.locuspark.api.service.report.export.format.CurrencyFormatter;
import com.locuspark.api.service.report.export.format.ReportCellFormatter;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvSectionWriter {

    public void write(CSVPrinter printer, ReportTable<?> table) throws IOException {
        List<ReportCellType> types = table.types(true);
        List<List<Object>> rows = table.renderRows(true);

        printer.println();
        printer.printRecord(table.title().toUpperCase());
        printer.printRecord(decoratedHeaders(table.headers(true), types));

        if (rows.isEmpty()) {
            printer.printRecord("Nenhum registro no período.");
            return;
        }
        for (List<Object> row : rows) {
            printer.printRecord(formatRow(row, types));
        }
    }

    private List<String> decoratedHeaders(List<String> headers, List<ReportCellType> types) {
        List<String> decorated = new ArrayList<>(headers.size());
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            boolean needsUnit = types.get(i) == ReportCellType.CURRENCY && !header.contains("(R$)");
            decorated.add(needsUnit ? header + " (R$)" : header);
        }
        return decorated;
    }

    private List<String> formatRow(List<Object> row, List<ReportCellType> types) {
        List<String> cells = new ArrayList<>(row.size());
        for (int i = 0; i < row.size(); i++) {
            cells.add(formatCell(types.get(i), row.get(i)));
        }
        return cells;
    }

    private String formatCell(ReportCellType type, Object value) {
        if (value == null) {
            return "—";
        }
        if (type == ReportCellType.CURRENCY) {
            return CurrencyFormatter.formatBare((BigDecimal) value);
        }
        return ReportCellFormatter.format(type, value);
    }
}
