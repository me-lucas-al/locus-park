package com.locuspark.api.service.report;

public record ReportDetailLimit(int maxRows) {

    public static final ReportDetailLimit JSON = new ReportDetailLimit(20_000);
    public static final ReportDetailLimit EXPORT = new ReportDetailLimit(20_000);

    public boolean exceededBy(int totalCount) {
        return totalCount > maxRows;
    }
}
