package com.locuspark.api.service.report.export.xlsx;

import com.locuspark.api.service.report.export.document.ReportCellType;

public final class XlsxColumnWidthPolicy {

    private XlsxColumnWidthPolicy() {
    }

    public static int poiUnitsFor(ReportCellType type) {
        int chars = switch (type) {
            case CURRENCY -> 14;
            case DECIMAL -> 12;
            case INTEGER -> 10;
            case PERCENT -> 10;
            case DATE -> 12;
            case DATE_TIME -> 18;
            case DURATION_MINUTES -> 12;
            case TEXT -> 28;
        };
        return chars * 256;
    }
}
