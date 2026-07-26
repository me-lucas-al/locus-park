package com.locuspark.api.enums;

public enum ReportExportFormat {

    PDF("application/pdf", "pdf"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"),
    CSV("text/csv", "csv");

    private final String contentType;
    private final String fileExtension;

    ReportExportFormat(String contentType, String fileExtension) {
        this.contentType = contentType;
        this.fileExtension = fileExtension;
    }

    public String contentType() {
        return contentType;
    }

    public String fileExtension() {
        return fileExtension;
    }
}
