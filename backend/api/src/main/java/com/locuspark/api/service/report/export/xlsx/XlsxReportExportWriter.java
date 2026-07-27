package com.locuspark.api.service.report.export.xlsx;

import com.locuspark.api.enums.ReportExportFormat;
import com.locuspark.api.service.report.export.ReportExportWriter;
import com.locuspark.api.service.report.export.document.ReportDocument;
import com.locuspark.api.service.report.export.document.ReportTable;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class XlsxReportExportWriter implements ReportExportWriter {

    private static final int FLUSH_WINDOW = 100;

    private final XlsxSummarySheetWriter summarySheetWriter;
    private final XlsxSheetWriter sheetWriter;

    public XlsxReportExportWriter(XlsxSummarySheetWriter summarySheetWriter, XlsxSheetWriter sheetWriter) {
        this.summarySheetWriter = summarySheetWriter;
        this.sheetWriter = sheetWriter;
    }

    @Override
    public ReportExportFormat format() {
        return ReportExportFormat.XLSX;
    }

    @Override
    public byte[] write(ReportDocument document) {
        SXSSFWorkbook workbook = new SXSSFWorkbook(new XSSFWorkbook(), FLUSH_WINDOW);
        workbook.setCompressTempFiles(true);
        try {
            XlsxStyleRegistry styles = new XlsxStyleRegistry(workbook);
            Sheet summary = workbook.createSheet("Resumo");
            summarySheetWriter.write(workbook, summary, document.header(), document.kpis(), document.logo());

            for (ReportTable<?> table : document.tables()) {
                Sheet sheet = workbook.createSheet(sheetNameFor(table.title()));
                sheetWriter.write(sheet, table, styles);
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Falha ao gerar XLSX do relatório.", ex);
        } finally {
            workbook.dispose();
        }
    }

    private String sheetNameFor(String tableTitle) {
        return switch (tableTitle) {
            case "Movimentação Diária" -> "Diário";
            case "Movimentação por Hora" -> "Por Hora";
            case "Detalhamento de Tickets" -> "Tickets";
            default -> tableTitle;
        };
    }
}
