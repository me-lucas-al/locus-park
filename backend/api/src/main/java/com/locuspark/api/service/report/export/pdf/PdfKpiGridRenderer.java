package com.locuspark.api.service.report.export.pdf;

import com.locuspark.api.service.report.export.document.ReportKpi;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.util.List;

@Component
public class PdfKpiGridRenderer {

    private static final int COLUMNS = 4;

    public void render(Document document, List<ReportKpi> kpis) throws DocumentException {
        if (kpis.isEmpty()) {
            return;
        }
        PdfPTable grid = new PdfPTable(COLUMNS);
        grid.setWidthPercentage(100);
        grid.setSpacingAfter(10);
        kpis.forEach(kpi -> grid.addCell(kpiCell(kpi)));

        int remainder = kpis.size() % COLUMNS;
        if (remainder != 0) {
            for (int i = remainder; i < COLUMNS; i++) {
                grid.addCell(blankCell());
            }
        }
        document.add(grid);
    }

    private PdfPCell kpiCell(ReportKpi kpi) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(6);
        cell.setBorderColor(new Color(226, 232, 240));
        cell.addElement(new Paragraph(kpi.label(), PdfFonts.KPI_LABEL_FONT));
        cell.addElement(new Paragraph(kpi.value(), PdfFonts.KPI_VALUE_FONT));
        return cell;
    }

    private PdfPCell blankCell() {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(new Color(226, 232, 240));
        return cell;
    }
}
