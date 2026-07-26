package com.locuspark.api.service.report.export;

import com.locuspark.api.dto.request.ReportFilter;
import com.locuspark.api.dto.response.ReportResponse;
import com.locuspark.api.enums.ReportExportFormat;
import com.locuspark.api.exception.BusinessException;
import com.locuspark.api.service.report.export.document.ReportDocument;
import com.locuspark.api.service.report.export.document.ReportDocumentAssembler;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReportExportService {

    private final Map<ReportExportFormat, ReportExportWriter> writers;
    private final ReportDocumentAssembler documentAssembler;
    private final ReportFileNameFactory fileNameFactory;

    public ReportExportService(List<ReportExportWriter> writers, ReportDocumentAssembler documentAssembler, ReportFileNameFactory fileNameFactory) {
        this.writers = writers.stream().collect(Collectors.toMap(ReportExportWriter::format, Function.identity()));
        this.documentAssembler = documentAssembler;
        this.fileNameFactory = fileNameFactory;
    }

    public ReportExportFile export(ReportResponse report, ReportFilter filter, ReportExportFormat format) {
        if (report.ticketsTruncated()) {
            throw new BusinessException("O período selecionado contém " + report.ticketCount()
                    + " tickets, acima do limite de exportação. Reduza o intervalo e tente novamente.");
        }
        ReportExportWriter writer = writers.get(format);
        ReportDocument document = documentAssembler.assemble(report);
        byte[] content = writer.write(document);
        String fileName = fileNameFactory.fileName(filter.from(), filter.to(), format);
        return new ReportExportFile(fileName, format, content);
    }
}
