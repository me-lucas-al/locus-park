package com.locuspark.api.service.report.export.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;

public final class CsvFormatProvider {

    private CsvFormatProvider() {
    }

    public static CSVFormat format() {
        return CSVFormat.EXCEL.builder()
                .setDelimiter(';')
                .setRecordSeparator("\r\n")
                .setQuoteMode(QuoteMode.MINIMAL)
                .get();
    }
}
