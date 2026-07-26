package com.locuspark.api.service.report.export.format;

public final class DurationFormatter {

    private DurationFormatter() {
    }

    public static String format(Long stayMinutes) {
        if (stayMinutes == null) {
            return "Em aberto";
        }
        long hours = stayMinutes / 60;
        long minutes = stayMinutes % 60;
        return hours > 0 ? hours + "h " + minutes + "min" : minutes + "min";
    }
}
