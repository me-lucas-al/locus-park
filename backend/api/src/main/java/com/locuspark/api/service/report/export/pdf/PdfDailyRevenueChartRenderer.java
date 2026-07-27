package com.locuspark.api.service.report.export.pdf;

import com.locuspark.api.service.report.export.document.ReportTable;
import com.locuspark.api.service.report.export.format.ReportDateFormatter;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Component
public class PdfDailyRevenueChartRenderer {

    private static final float WIDTH = 700f;
    private static final float HEIGHT = 140f;
    private static final int MAX_DAYS = 62;

    public void render(Document document, PdfWriter writer, ReportTable<?> dailyTable) throws DocumentException {
        List<List<Object>> rows = dailyTable.renderRows(true);
        if (rows.isEmpty() || rows.size() > MAX_DAYS) {
            return;
        }
        BigDecimal max = rows.stream().map(row -> (BigDecimal) row.get(3)).max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
        if (max.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        document.add(new Paragraph("Faturamento Diário", PdfFonts.KPI_LABEL_FONT));
        float top = writer.getVerticalPosition(true) - 4;
        float bottom = top - HEIGHT;
        drawChart(writer.getDirectContent(), document.left(), bottom, rows, max);
        reserveSpace(document, top - bottom + 10);
    }

    private void drawChart(PdfContentByte content, float x, float y, List<List<Object>> rows, BigDecimal max) {
        content.setLineWidth(0.5f);
        content.setColorFill(new Color(226, 232, 240));
        for (int i = 1; i <= 3; i++) {
            float gridY = y + HEIGHT * i / 4f;
            content.moveTo(x, gridY);
            content.lineTo(x + WIDTH, gridY);
        }
        content.stroke();

        int barCount = rows.size();
        float gap = WIDTH / barCount;
        float barWidth = gap * 0.7f;
        content.setColorFill(new Color(37, 99, 235));
        for (int i = 0; i < barCount; i++) {
            BigDecimal revenue = (BigDecimal) rows.get(i).get(3);
            float barHeight = revenue.divide(max, 6, RoundingMode.HALF_UP).floatValue() * HEIGHT;
            content.rectangle(x + i * gap + (gap - barWidth) / 2, y, barWidth, Math.max(barHeight, 0));
        }
        content.fill();

        content.setFontAndSize(PdfFonts.FOOTER_FONT.getCalculatedBaseFont(false), 6);
        for (int i = 0; i < barCount; i += 5) {
            LocalDate date = (LocalDate) rows.get(i).get(0);
            content.showTextAligned(Element.ALIGN_CENTER, ReportDateFormatter.formatDate(date).substring(0, 5), x + i * gap + gap / 2, y - 10, 0);
        }
    }

    private void reserveSpace(Document document, float height) throws DocumentException {
        PdfPTable spacer = new PdfPTable(1);
        spacer.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setFixedHeight(height);
        spacer.addCell(cell);
        document.add(spacer);
    }
}
