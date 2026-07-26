package com.locuspark.api.service.report.export.document;

import com.locuspark.api.dto.response.ReportResponse;

public interface ReportSectionFactory {
    ReportTable<?> build(ReportResponse report);
}
