package com.locuspark.api.service.report.export.xlsx;

import com.locuspark.api.service.report.export.document.ReportCellType;
import com.locuspark.api.service.report.export.document.ReportTable;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class XlsxSheetWriter {

    public void write(Sheet sheet, ReportTable<?> table, XlsxStyleRegistry styles) {
        List<String> headers = table.headers(true);
        List<ReportCellType> types = table.types(true);
        List<List<Object>> rows = table.renderRows(true);

        writeHeaderRow(sheet, headers, styles);
        setColumnWidths(sheet, types);

        if (rows.isEmpty()) {
            sheet.createRow(1).createCell(0).setCellValue("Nenhum registro no período.");
            return;
        }

        sheet.createFreezePane(0, 1);
        sheet.setAutoFilter(new CellRangeAddress(0, rows.size(), 0, headers.size() - 1));

        for (int i = 0; i < rows.size(); i++) {
            writeDataRow(sheet, i + 1, rows.get(i), types, styles);
        }
    }

    private void writeHeaderRow(Sheet sheet, List<String> headers, XlsxStyleRegistry styles) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(styles.headerStyle());
        }
    }

    private void setColumnWidths(Sheet sheet, List<ReportCellType> types) {
        for (int i = 0; i < types.size(); i++) {
            sheet.setColumnWidth(i, XlsxColumnWidthPolicy.poiUnitsFor(types.get(i)));
        }
    }

    private void writeDataRow(Sheet sheet, int rowIndex, List<Object> values, List<ReportCellType> types, XlsxStyleRegistry styles) {
        Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < values.size(); i++) {
            XlsxCellWriter.write(row.createCell(i), types.get(i), values.get(i), styles.dataStyle(types.get(i)));
        }
    }
}
