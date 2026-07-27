package com.locuspark.api.service.report.export.xlsx;

import com.locuspark.api.service.report.export.document.ReportCellType;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.EnumMap;
import java.util.Map;

public class XlsxStyleRegistry {

    private final Map<ReportCellType, CellStyle> dataStyles = new EnumMap<>(ReportCellType.class);
    private final CellStyle headerStyle;

    public XlsxStyleRegistry(Workbook workbook) {
        DataFormat dataFormat = workbook.getCreationHelper().createDataFormat();
        for (ReportCellType type : ReportCellType.values()) {
            CellStyle style = workbook.createCellStyle();
            String pattern = numberFormatPattern(type);
            if (pattern != null) {
                style.setDataFormat(dataFormat.getFormat(pattern));
            }
            dataStyles.put(type, style);
        }
        headerStyle = buildHeaderStyle(workbook);
    }

    public CellStyle dataStyle(ReportCellType type) {
        return dataStyles.get(type);
    }

    public CellStyle headerStyle() {
        return headerStyle;
    }

    private CellStyle buildHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private String numberFormatPattern(ReportCellType type) {
        return switch (type) {
            case CURRENCY -> "R$ #,##0.00";
            case DECIMAL -> "#,##0.00";
            case INTEGER -> "#,##0";
            case PERCENT -> "0.0%";
            case DATE -> "dd/mm/yyyy";
            case DATE_TIME -> "dd/mm/yyyy hh:mm";
            case DURATION_MINUTES, TEXT -> null;
        };
    }
}
