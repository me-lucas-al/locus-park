package com.locuspark.api.service.report.export.format;

import com.locuspark.api.types.Plate;

public final class PlateFormatter {

    private PlateFormatter() {
    }

    public static String format(String rawPlate) {
        if (rawPlate == null || rawPlate.isBlank()) {
            return "—";
        }
        try {
            return new Plate(rawPlate).toString();
        } catch (RuntimeException ex) {
            return rawPlate;
        }
    }
}
