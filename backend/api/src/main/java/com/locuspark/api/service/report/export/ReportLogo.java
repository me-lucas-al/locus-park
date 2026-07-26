package com.locuspark.api.service.report.export;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
public class ReportLogo {

    private static final String PATH = "reports/locus-park-logo.png";

    private byte[] bytes = new byte[0];

    @PostConstruct
    void load() {
        ClassPathResource resource = new ClassPathResource(PATH);
        if (!resource.exists()) {
            log.warn("Logo do relatório não encontrado em {}; exportações seguirão sem logo.", PATH);
            return;
        }
        try (InputStream in = resource.getInputStream()) {
            bytes = in.readAllBytes();
        } catch (IOException ex) {
            log.warn("Falha ao carregar o logo do relatório em {}: {}", PATH, ex.getMessage());
        }
    }

    public byte[] bytes() {
        return bytes;
    }
}
