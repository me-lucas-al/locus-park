package com.locuspark.api.service.report.export.format;

import com.locuspark.api.enums.PaymentMethod;

public final class PaymentMethodLabel {

    private PaymentMethodLabel() {
    }

    public static String label(PaymentMethod method) {
        if (method == null) {
            return "—";
        }
        return switch (method) {
            case DINHEIRO -> "Dinheiro";
            case PIX -> "Pix";
            case CARD_CREDIT -> "Cartão de Crédito";
            case CARD_DEBIT -> "Cartão de Débito";
        };
    }
}
