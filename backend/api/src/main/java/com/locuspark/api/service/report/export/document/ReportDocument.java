package com.locuspark.api.service.report.export.document;

import java.util.List;

public record ReportDocument(ReportDocumentHeader header, List<ReportKpi> kpis, List<ReportTable<?>> tables, byte[] logo) {

    public static final ReportDocument EMPTY = new ReportDocument(ReportDocumentHeader.EMPTY, List.of(), List.of(), new byte[0]);
}
