package com.locuspark.api.service;

import com.locuspark.api.entity.PricingConfiguration;
import com.locuspark.api.entity.TariffConfiguration;
import com.locuspark.api.entity.Ticket;
import com.locuspark.api.enums.VehicleType;
import com.locuspark.api.service.payment.GrossStayChargeCalculator;
import com.locuspark.api.service.payment.PartnershipDiscountCalculator;
import com.locuspark.api.service.payment.StayCharge;
import com.locuspark.api.service.payment.TolerancePolicy;
import com.locuspark.api.service.payment.VehicleTypeMultiplierResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final ZoneId PATIO_ZONE = ZoneId.of("America/Sao_Paulo");

    private final TolerancePolicy tolerancePolicy;
    private final GrossStayChargeCalculator grossStayChargeCalculator;
    private final PartnershipDiscountCalculator partnershipDiscountCalculator;
    private final VehicleTypeMultiplierResolver vehicleTypeMultiplierResolver;

    public StayCharge calculateStayCharge(Ticket ticket, Instant exitTime, TariffConfiguration tariff, PricingConfiguration pricing) {
        Instant entryTime = ticket.getEnteredAt();

        if (exitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException("A data de saída não pode ser menor que a data de entrada.");
        }

        long stayMinutes = Duration.between(entryTime, exitTime).toMinutes();

        if (tolerancePolicy.isWithinTolerance(stayMinutes, tariff)) {
            return StayCharge.free();
        }

        LocalDate entryDate = entryTime.atZone(PATIO_ZONE).toLocalDate();
        LocalDate exitDate = exitTime.atZone(PATIO_ZONE).toLocalDate();
        boolean crossedDate = entryDate.isBefore(exitDate);
        var baseGross = grossStayChargeCalculator.grossAmount(stayMinutes, crossedDate, tariff, pricing);

        UUID companyId = ticket.getCompany() != null ? ticket.getCompany().getId() : null;
        VehicleType vehicleType = ticket.getVehicle() != null ? ticket.getVehicle().getType() : null;
        BigDecimal multiplier = vehicleTypeMultiplierResolver.resolve(companyId, vehicleType);
        BigDecimal gross = baseGross.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);

        var net = ticket.getPartnership() != null
                ? partnershipDiscountCalculator.netAmount(gross, ticket.getPartnership(), stayMinutes, tariff)
                : gross;

        return StayCharge.of(gross, net);
    }
}
