package com.locuspark.api.service.report.export.document;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReportDocumentHeader(
        String companyName,
        String companyCnpj,
        int totalSpots,
        LocalDate periodFrom,
        LocalDate periodTo,
        long periodDays,
        LocalDateTime generatedAt,
        long ticketCount,
        boolean ticketsTruncated
) {

    public static final ReportDocumentHeader EMPTY = new ReportDocumentHeader(
            "—", "—", 0, LocalDate.EPOCH, LocalDate.EPOCH, 0, LocalDateTime.MIN, 0, false);
}
