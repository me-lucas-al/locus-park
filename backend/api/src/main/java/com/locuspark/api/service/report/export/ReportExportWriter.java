package com.locuspark.api.service.report.export;

import com.locuspark.api.enums.ReportExportFormat;
import com.locuspark.api.service.report.export.document.ReportDocument;

public interface ReportExportWriter {
    ReportExportFormat format();
    byte[] write(ReportDocument document);
}
