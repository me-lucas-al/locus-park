package com.locuspark.api.service.report.export;

import com.locuspark.api.enums.ReportExportFormat;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ReportFileNameFactory {

    public String fileName(LocalDate from, LocalDate to, ReportExportFormat format) {
        return "relatorio-locus-park-" + from + "-a-" + to + "." + format.fileExtension();
    }
}
