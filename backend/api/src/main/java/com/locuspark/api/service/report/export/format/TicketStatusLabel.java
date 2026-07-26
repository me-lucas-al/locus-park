package com.locuspark.api.service.report.export.format;

import com.locuspark.api.enums.TicketStatus;

public final class TicketStatusLabel {

    private TicketStatusLabel() {
    }

    public static String label(TicketStatus status) {
        if (status == null) {
            return "—";
        }
        return switch (status) {
            case ACTIVE -> "Ativo";
            case PAID -> "Pago";
        };
    }
}
