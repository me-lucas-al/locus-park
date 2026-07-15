package com.locuspark.api.service;

import com.locuspark.api.entity.Partnership;
import com.locuspark.api.entity.PricingConfiguration;
import com.locuspark.api.entity.TariffConfiguration;
import com.locuspark.api.entity.Ticket;
import com.locuspark.api.enums.DiscountType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class PaymentService {

    public BigDecimal calculateStayAmount(Ticket ticket, Instant exitTime, TariffConfiguration tariff, PricingConfiguration pricing) {
        Instant entryTime = ticket.getEnteredAt();

        if (exitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException("A data de saída não pode ser menor que a data de entrada.");
        }

        // 1. Cálculo de tempo total
        Duration duration = Duration.between(entryTime, exitTime);
        long totalMinutes = duration.toMinutes();

        // 2. Aplicação do tempo de tolerância do pátio
        int tolerance = tariff.getToleranceMinutes() != null ? tariff.getToleranceMinutes() : 0;
        if (totalMinutes <= tolerance) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        // 3. Conversão de horas arredondando frações para cima (Cobrança por hora/fração comercial)
        long hoursToCharge = (long) Math.ceil(totalMinutes / 60.0);

        BigDecimal baseAmount = BigDecimal.ZERO;

        // 4. Regra de Diária (Se ultrapassar o gatilho configurado, vira cobrança de diária fixa)
        int dailyTrigger = pricing.getDailyTriggerHours() != null ? pricing.getDailyTriggerHours() : 24;
        if (hoursToCharge >= dailyTrigger) {
            baseAmount = pricing.getDailyValue() != null ? pricing.getDailyValue() : BigDecimal.ZERO;
        } else {
            // Cobrança horária normal: Primeira hora + Frações adicionais
            BigDecimal firstHourValue = tariff.getFirstHourValue() != null ? tariff.getFirstHourValue() : BigDecimal.ZERO;
            BigDecimal additionalFractionValue = tariff.getAdditionalFractionValue() != null ? tariff.getAdditionalFractionValue() : BigDecimal.ZERO;

            if (hoursToCharge <= 1) {
                baseAmount = firstHourValue;
            } else {
                long additionalHours = hoursToCharge - 1;
                baseAmount = firstHourValue.add(additionalFractionValue.multiply(BigDecimal.valueOf(additionalHours)));
            }
        }

        // 5. Aplicação de Taxa de Pernoite se houver virada de dia (opcional no pátio)
        ZoneId zoneId = ZoneId.of("America/Sao_Paulo");
        LocalDate entryDate = entryTime.atZone(zoneId).toLocalDate();
        LocalDate exitDate = exitTime.atZone(zoneId).toLocalDate();
        if (entryDate.isBefore(exitDate) && tariff.getOvernightFee() != null) {
            baseAmount = baseAmount.add(tariff.getOvernightFee());
        }

        // 6. Verificação de Convênios/Parcerias vinculadas ao Ticket
        if (ticket.getPartnership() != null) {
            baseAmount = applyDiscount(baseAmount, ticket.getPartnership(), totalMinutes, tariff);
        }

        // Garante que o valor final nunca seja negativo e fixa a escala em 2 casas decimais
        if (baseAmount.compareTo(BigDecimal.ZERO) < 0) {
            baseAmount = BigDecimal.ZERO;
        }

        return baseAmount.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal applyDiscount(BigDecimal currentAmount, Partnership partnership, long totalMinutes, TariffConfiguration tariff) {
        DiscountType type = partnership.getDiscountType();
        BigDecimal discountValue = partnership.getValue();

        if (type == null || discountValue == null || discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            return currentAmount;
        }

        switch (type) {
            case PERCENTAGE:
                // Desconto em % (Ex: 10% de desconto)
                BigDecimal discountFactor = discountValue.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                BigDecimal percentageDiscount = currentAmount.multiply(discountFactor);
                return currentAmount.subtract(percentageDiscount);

            case FIXED_VALUE:
                // Desconto de valor nominal fixo (Ex: Desconto de R$ 5,00)
                return currentAmount.subtract(discountValue);

            case FREE_HOURS:
                // Isenção de horas (Ex: Primeiras 2 horas grátis)
                long freeMinutes = discountValue.longValue() * 60;
                if (totalMinutes <= freeMinutes) {
                    return BigDecimal.ZERO;
                }

                // Recalcula o tempo restante cobrável subtraindo as horas gratuitas
                long billableMinutes = totalMinutes - freeMinutes;
                long billableHours = (long) Math.ceil(billableMinutes / 60.0);

                BigDecimal firstHourValue = tariff.getFirstHourValue() != null ? tariff.getFirstHourValue() : BigDecimal.ZERO;
                BigDecimal additionalFractionValue = tariff.getAdditionalFractionValue() != null ? tariff.getAdditionalFractionValue() : BigDecimal.ZERO;

                if (billableHours <= 1) {
                    return firstHourValue;
                } else {
                    return firstHourValue.add(additionalFractionValue.multiply(BigDecimal.valueOf(billableHours - 1)));
                }

            default:
                return currentAmount;
        }
    }
}