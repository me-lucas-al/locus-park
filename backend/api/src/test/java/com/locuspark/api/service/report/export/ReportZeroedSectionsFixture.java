package com.locuspark.api.service.report.export;

import com.locuspark.api.dto.response.report.DailySummaryResponse;
import com.locuspark.api.dto.response.report.HourlySummaryResponse;
import com.locuspark.api.dto.response.report.PaymentMethodSummaryResponse;
import com.locuspark.api.dto.response.report.VehicleTypeSummaryResponse;
import com.locuspark.api.enums.PaymentMethod;
import com.locuspark.api.enums.VehicleType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

final class ReportZeroedSectionsFixture {

    private ReportZeroedSectionsFixture() {
    }

    static List<DailySummaryResponse> daily(LocalDate from, LocalDate to) {
        return from.datesUntil(to.plusDays(1))
                .map(date -> new DailySummaryResponse(date, 0, 0, BigDecimal.ZERO, BigDecimal.ZERO))
                .toList();
    }

    static List<HourlySummaryResponse> hourly() {
        return IntStream.range(0, 24).mapToObj(hour -> new HourlySummaryResponse(hour, 0, 0, BigDecimal.ZERO)).toList();
    }

    static List<PaymentMethodSummaryResponse> paymentMethods() {
        return Arrays.stream(PaymentMethod.values())
                .map(method -> new PaymentMethodSummaryResponse(method, 0, BigDecimal.ZERO, 0.0))
                .toList();
    }

    static List<VehicleTypeSummaryResponse> vehicleTypes() {
        return Arrays.stream(VehicleType.values())
                .map(type -> new VehicleTypeSummaryResponse(type, 0, BigDecimal.ZERO, 0.0))
                .toList();
    }
}
