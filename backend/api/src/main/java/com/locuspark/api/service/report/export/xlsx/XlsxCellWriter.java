package com.locuspark.api.service.report.export.xlsx;

import com.locuspark.api.service.report.export.document.ReportCellType;
import com.locuspark.api.service.report.export.format.DurationFormatter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class XlsxCellWriter {

    private XlsxCellWriter() {
    }

    public static void write(Cell cell, ReportCellType type, Object value, CellStyle style) {
        cell.setCellStyle(style);
        if (value == null) {
            cell.setCellValue("—");
            return;
        }
        switch (type) {
            case TEXT -> cell.setCellValue(value.toString());
            case INTEGER, DECIMAL -> cell.setCellValue(((Number) value).doubleValue());
            case CURRENCY -> cell.setCellValue(((BigDecimal) value).doubleValue());
            case PERCENT -> cell.setCellValue(((Number) value).doubleValue() / 100d);
            case DATE -> cell.setCellValue((LocalDate) value);
            case DATE_TIME -> cell.setCellValue((LocalDateTime) value);
            case DURATION_MINUTES -> cell.setCellValue(DurationFormatter.format(((Number) value).longValue()));
        }
    }
}
