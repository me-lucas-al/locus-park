package com.locuspark.api.config;

import com.locuspark.api.enums.ReportExportFormat;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ReportExportFormatConverter implements Converter<String, ReportExportFormat> {

    @Override
    public ReportExportFormat convert(@NonNull String source) {
        return ReportExportFormat.valueOf(source.trim().toUpperCase());
    }
}
