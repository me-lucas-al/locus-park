package com.locuspark.api.config;

import com.locuspark.api.service.report.export.ReportExportWriter;
import com.locuspark.api.service.report.export.document.ReportDocument;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.crac.Context;
import org.crac.Core;
import org.crac.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportExportWarmup implements Resource {

    private final List<ReportExportWriter> writers;

    @PostConstruct
    void register() {
        Core.getGlobalContext().register(this);
    }

    @Override
    public void beforeCheckpoint(Context<? extends Resource> context) {
        for (ReportExportWriter writer : writers) {
            try {
                writer.write(ReportDocument.EMPTY);
            } catch (Exception ex) {
                log.warn("Falha ao aquecer o writer de exportação {} no SnapStart: {}", writer.format(), ex.getMessage());
            }
        }
    }

    @Override
    public void afterRestore(Context<? extends Resource> context) {
    }
}
