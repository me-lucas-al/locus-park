package com.locuspark.api.service.report.export;

import com.locuspark.api.enums.ReportExportFormat;

public record ReportExportFile(String fileName, ReportExportFormat format, byte[] content) {
}
