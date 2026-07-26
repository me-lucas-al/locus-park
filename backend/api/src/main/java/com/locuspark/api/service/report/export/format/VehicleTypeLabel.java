package com.locuspark.api.service.report.export.format;

import com.locuspark.api.enums.VehicleType;

public final class VehicleTypeLabel {

    private VehicleTypeLabel() {
    }

    public static String label(VehicleType type) {
        if (type == null) {
            return "—";
        }
        return switch (type) {
            case CAR -> "Carro";
            case MOTORCYCLE -> "Moto";
            case VAN -> "Van";
            case TRUCK -> "Caminhão";
        };
    }
}
