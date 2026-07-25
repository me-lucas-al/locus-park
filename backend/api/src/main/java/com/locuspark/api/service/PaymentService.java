package com.locuspark.api.service;

import com.locuspark.api.entity.PricingConfiguration;
import com.locuspark.api.entity.TariffConfiguration;
import com.locuspark.api.entity.Ticket;
import com.locuspark.api.service.payment.GrossStayChargeCalculator;
import com.locuspark.api.service.payment.PartnershipDiscountCalculator;
import com.locuspark.api.service.payment.StayCharge;
import com.locuspark.api.service.payment.TolerancePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final TolerancePolicy tolerancePolicy;
    private final GrossStayChargeCalculator grossStayChargeCalculator;
    private final PartnershipDiscountCalculator partnershipDiscountCalculator;

    public StayCharge calculateStayCharge(Ticket ticket, LocalDateTime exitTime, TariffConfiguration tariff, PricingConfiguration pricing) {
        LocalDateTime entryTime = ticket.getEnteredAt();

        if (exitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException("A data de saída não pode ser menor que a data de entrada.");
        }

        long stayMinutes = Duration.between(entryTime, exitTime).toMinutes();

        if (tolerancePolicy.isWithinTolerance(stayMinutes, tariff)) {
            return StayCharge.free();
        }

        boolean crossedDate = entryTime.toLocalDate().isBefore(exitTime.toLocalDate());
        var gross = grossStayChargeCalculator.grossAmount(stayMinutes, crossedDate, tariff, pricing);

        var net = ticket.getPartnership() != null
                ? partnershipDiscountCalculator.netAmount(gross, ticket.getPartnership(), stayMinutes, tariff)
                : gross;

        return StayCharge.of(gross, net);
    }
}
