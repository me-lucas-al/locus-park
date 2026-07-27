package com.locuspark.api.service.report.export.pdf;

import com.locuspark.api.service.report.export.document.ReportCellType;
import com.locuspark.api.service.report.export.document.ReportTable;
import com.locuspark.api.service.report.export.format.ReportCellFormatter;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.util.List;

@Component
public class PdfTableRenderer {

    public void render(Document document, ReportTable<?> table) throws DocumentException {
        List<String> headers = table.headers(false);
        List<ReportCellType> types = table.types(false);
        List<List<Object>> rows = table.renderRows(false);
        int columnCount = headers.size();

        PdfPTable pdfTable = new PdfPTable(columnCount);
        pdfTable.setWidthPercentage(100);
        pdfTable.setSpacingBefore(6);
        pdfTable.setSpacingAfter(10);
        pdfTable.setHeaderRows(2);

        addTitleRow(pdfTable, table.title(), columnCount);
        addHeaderRow(pdfTable, headers);

        if (rows.isEmpty()) {
            addEmptyRow(pdfTable, columnCount);
        } else {
            for (int i = 0; i < rows.size(); i++) {
                addBodyRow(pdfTable, rows.get(i), types, i % 2 == 1);
            }
        }
        document.add(pdfTable);
    }

    private void addTitleRow(PdfPTable table, String title, int columnCount) {
        PdfPCell cell = new PdfPCell(new Phrase(title, PdfFonts.SECTION_TITLE_FONT));
        cell.setColspan(columnCount);
        cell.setBackgroundColor(PdfFonts.HEADER_BACKGROUND);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addHeaderRow(PdfPTable table, List<String> headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, PdfFonts.TABLE_HEADER_FONT));
            cell.setBackgroundColor(new Color(71, 85, 105));
            cell.setPadding(4);
            table.addCell(cell);
        }
    }

    private void addEmptyRow(PdfPTable table, int columnCount) {
        PdfPCell cell = new PdfPCell(new Phrase("Nenhum registro no período.", PdfFonts.TABLE_CELL_FONT));
        cell.setColspan(columnCount);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addBodyRow(PdfPTable table, List<Object> row, List<ReportCellType> types, boolean zebra) {
        for (int i = 0; i < row.size(); i++) {
            String text = ReportCellFormatter.format(types.get(i), row.get(i));
            PdfPCell cell = new PdfPCell(new Phrase(text, PdfFonts.TABLE_CELL_FONT));
            cell.setPadding(4);
            if (zebra) {
                cell.setBackgroundColor(PdfFonts.ZEBRA_BACKGROUND);
            }
            table.addCell(cell);
        }
    }
}
