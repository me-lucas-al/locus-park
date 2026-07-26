package com.locuspark.api.service.report.export.document;

import java.util.List;

public record ReportTable<T>(String title, List<ReportColumn<T>> columns, List<T> rows) {

    public List<ReportColumn<T>> columns(boolean includeDataOnly) {
        if (includeDataOnly) {
            return columns;
        }
        return columns.stream().filter(column -> column.scope() == ReportColumnScope.ALL).toList();
    }

    public List<String> headers(boolean includeDataOnly) {
        return columns(includeDataOnly).stream().map(ReportColumn::header).toList();
    }

    public List<ReportCellType> types(boolean includeDataOnly) {
        return columns(includeDataOnly).stream().map(ReportColumn::type).toList();
    }

    public List<List<Object>> renderRows(boolean includeDataOnly) {
        List<ReportColumn<T>> visibleColumns = columns(includeDataOnly);
        return rows.stream()
                .map(row -> visibleColumns.stream().map(column -> column.extractor().apply(row)).toList())
                .toList();
    }
}
