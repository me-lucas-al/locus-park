package com.locuspark.api.service.report.export.csv;

import com.locuspark.api.enums.ReportExportFormat;
import com.locuspark.api.service.report.export.ReportExportWriter;
import com.locuspark.api.service.report.export.document.ReportDocument;
import com.locuspark.api.service.report.export.document.ReportDocumentHeader;
import com.locuspark.api.service.report.export.document.ReportKpi;
import com.locuspark.api.service.report.export.document.ReportTable;
import com.locuspark.api.service.report.export.format.DocumentFormatter;
import com.locuspark.api.service.report.export.format.ReportDateFormatter;
import com.locuspark.api.service.report.export.format.ReportLocale;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CsvReportExportWriter implements ReportExportWriter {

    private final CsvSectionWriter sectionWriter;

    @Override
    public ReportExportFormat format() {
        return ReportExportFormat.CSV;
    }

    @Override
    public byte[] write(ReportDocument document) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (Writer bomWriter = Utf8BomWriter.wrap(output);
                CSVPrinter printer = new CSVPrinter(bomWriter, CsvFormatProvider.format())) {
            writePreamble(printer, document.header());
            writeResumo(printer, document.kpis());
            for (ReportTable<?> table : document.tables()) {
                sectionWriter.write(printer, table);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Falha ao gerar CSV do relatório.", ex);
        }
        return output.toByteArray();
    }

    private void writePreamble(CSVPrinter printer, ReportDocumentHeader header) throws IOException {
        printer.printRecord("Relatório Locus Park");
        printer.printRecord("Empresa", header.companyName());
        printer.printRecord("CNPJ", DocumentFormatter.formatCnpj(header.companyCnpj()));
        printer.printRecord("Vagas", header.totalSpots());
        printer.printRecord("Período", ReportDateFormatter.formatDate(header.periodFrom()) + " a " + ReportDateFormatter.formatDate(header.periodTo()));
        printer.printRecord("Gerado em", ReportDateFormatter.formatDateTime(header.generatedAt()) + " (" + ReportLocale.TIME_ZONE_LABEL + ")");
    }

    private void writeResumo(CSVPrinter printer, List<ReportKpi> kpis) throws IOException {
        printer.println();
        printer.printRecord("RESUMO");
        for (ReportKpi kpi : kpis) {
            printer.printRecord(kpi.label(), stripCurrencyPrefix(kpi.value()));
        }
    }

    private String stripCurrencyPrefix(String value) {
        return value.startsWith("R$ ") ? value.substring(3) : value;
    }
}
