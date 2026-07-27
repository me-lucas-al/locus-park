package com.locuspark.api.service.report.export.xlsx;

import com.locuspark.api.service.report.export.document.ReportCellType;
import com.locuspark.api.service.report.export.document.ReportDocumentHeader;
import com.locuspark.api.service.report.export.document.ReportKpi;
import com.locuspark.api.service.report.export.format.DocumentFormatter;
import com.locuspark.api.service.report.export.format.ReportDateFormatter;
import com.locuspark.api.service.report.export.format.ReportLocale;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class XlsxSummarySheetWriter {

    public void write(Workbook workbook, Sheet sheet, ReportDocumentHeader header, List<ReportKpi> kpis, byte[] logo) {
        int rowIndex = writeHeaderInfo(sheet, header);
        writeKpis(sheet, kpis, rowIndex + 1);

        sheet.setColumnWidth(0, XlsxColumnWidthPolicy.poiUnitsFor(ReportCellType.TEXT));
        sheet.setColumnWidth(1, XlsxColumnWidthPolicy.poiUnitsFor(ReportCellType.TEXT));

        if (logo.length > 0) {
            addLogo(workbook, sheet, logo);
        }
    }

    private int writeHeaderInfo(Sheet sheet, ReportDocumentHeader header) {
        int rowIndex = 0;
        writeRow(sheet, rowIndex++, "Empresa", header.companyName());
        writeRow(sheet, rowIndex++, "CNPJ", DocumentFormatter.formatCnpj(header.companyCnpj()));
        writeRow(sheet, rowIndex++, "Vagas", String.valueOf(header.totalSpots()));
        writeRow(sheet, rowIndex++, "Período", ReportDateFormatter.formatDate(header.periodFrom()) + " a " + ReportDateFormatter.formatDate(header.periodTo()));
        writeRow(sheet, rowIndex++, "Gerado em", ReportDateFormatter.formatDateTime(header.generatedAt()) + " (" + ReportLocale.TIME_ZONE_LABEL + ")");
        return rowIndex;
    }

    private void writeKpis(Sheet sheet, List<ReportKpi> kpis, int startRow) {
        for (int i = 0; i < kpis.size(); i++) {
            writeRow(sheet, startRow + i, kpis.get(i).label(), kpis.get(i).value());
        }
    }

    private void writeRow(Sheet sheet, int rowIndex, String label, String value) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
    }

    private void addLogo(Workbook workbook, Sheet sheet, byte[] logo) {
        int pictureIndex = workbook.addPicture(logo, Workbook.PICTURE_TYPE_PNG);
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        ClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
        anchor.setCol1(3);
        anchor.setRow1(0);
        anchor.setCol2(6);
        anchor.setRow2(6);
        drawing.createPicture(anchor, pictureIndex);
    }
}
