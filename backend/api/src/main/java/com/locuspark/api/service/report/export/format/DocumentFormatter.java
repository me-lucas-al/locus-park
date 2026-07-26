package com.locuspark.api.service.report.export.format;

import com.locuspark.api.types.Cnpj;
import com.locuspark.api.types.Cpf;

public final class DocumentFormatter {

    private DocumentFormatter() {
    }

    public static String formatCpf(String rawCpf) {
        if (rawCpf == null || rawCpf.isBlank()) {
            return "—";
        }
        try {
            return new Cpf(rawCpf).toString();
        } catch (RuntimeException ex) {
            return rawCpf;
        }
    }

    public static String formatCnpj(String rawCnpj) {
        if (rawCnpj == null || rawCnpj.isBlank()) {
            return "—";
        }
        try {
            return new Cnpj(rawCnpj).toString();
        } catch (RuntimeException ex) {
            return rawCnpj;
        }
    }
}
