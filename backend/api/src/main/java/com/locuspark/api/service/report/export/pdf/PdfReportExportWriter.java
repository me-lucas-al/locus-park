package com.locuspark.api.service.report.export.pdf;

import com.locuspark.api.enums.ReportExportFormat;
import com.locuspark.api.service.report.export.ReportExportWriter;
import com.locuspark.api.service.report.export.document.ReportDocument;
import com.locuspark.api.service.report.export.document.ReportTable;
import com.locuspark.api.service.report.export.document.section.DailySection;
import com.locuspark.api.service.report.export.format.ReportDateFormatter;
import com.locuspark.api.service.report.export.format.ReportLocale;
import lombok.RequiredArgsConstructor;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
@RequiredArgsConstructor
public class PdfReportExportWriter implements ReportExportWriter {

    private final PdfDocumentFactory documentFactory;
    private final PdfHeaderRenderer headerRenderer;
    private final PdfKpiGridRenderer kpiGridRenderer;
    private final PdfDailyRevenueChartRenderer chartRenderer;
    private final PdfTableRenderer tableRenderer;

    @Override
    public ReportExportFormat format() {
        return ReportExportFormat.PDF;
    }

    @Override
    public byte[] write(ReportDocument document) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Document pdf = documentFactory.create();
        boolean opened = false;
        try {
            String generatedAtLabel = ReportDateFormatter.formatDateTime(document.header().generatedAt()) + " (" + ReportLocale.TIME_ZONE_LABEL + ")";
            PdfWriter writer = documentFactory.attachWriter(pdf, output, new PdfPageFooterEvent(generatedAtLabel));
            pdf.open();
            opened = true;

            headerRenderer.render(pdf, document.header(), document.logo());
            kpiGridRenderer.render(pdf, document.kpis());

            for (ReportTable<?> table : document.tables()) {
                if (table.title().equals(DailySection.TITLE)) {
                    chartRenderer.render(pdf, writer, table);
                }
                tableRenderer.render(pdf, table);
            }
        } catch (DocumentException ex) {
            throw new IllegalStateException("Falha ao gerar PDF do relatório.", ex);
        } finally {
            if (opened) {
                pdf.close();
            }
        }
        return output.toByteArray();
    }
}
