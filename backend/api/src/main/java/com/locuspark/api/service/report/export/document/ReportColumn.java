package com.locuspark.api.service.report.export.document;

import java.util.function.Function;

public record ReportColumn<T>(String header, ReportCellType type, ReportColumnScope scope, Function<T, Object> extractor) {

    public static <T> ReportColumn<T> of(String header, ReportCellType type, Function<T, Object> extractor) {
        return new ReportColumn<>(header, type, ReportColumnScope.ALL, extractor);
    }

    public static <T> ReportColumn<T> dataOnly(String header, ReportCellType type, Function<T, Object> extractor) {
        return new ReportColumn<>(header, type, ReportColumnScope.DATA_ONLY, extractor);
    }
}
