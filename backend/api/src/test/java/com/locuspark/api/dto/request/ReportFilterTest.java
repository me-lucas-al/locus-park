package com.locuspark.api.dto.request;

import com.locuspark.api.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes de ReportFilter")
class ReportFilterTest {

    @Test
    @DisplayName("Sem datas deve usar hoje como fim e os 30 dias anteriores como inicio")
    void defaultsToLastThirtyDaysEndingToday() {
        ReportFilter filter = new ReportFilter(null, null);

        assertThat(filter.to()).isEqualTo(LocalDate.now());
        assertThat(filter.from()).isEqualTo(LocalDate.now().minusDays(29));
        assertThat(filter.days()).isEqualTo(30);
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando a data inicial for posterior à final")
    void throwsWhenFromIsAfterTo() {
        assertThatThrownBy(() -> new ReportFilter(LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 1)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Deve aceitar uma diferença de 365 dias entre from e to")
    void acceptsThreeHundredSixtyFiveDaysOfDifference() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = from.plusDays(365);

        ReportFilter filter = new ReportFilter(from, to);

        assertThat(filter.days()).isEqualTo(366);
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando a diferença entre from e to atingir 366 dias")
    void throwsWhenDifferenceReachesThreeHundredSixtySixDays() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = from.plusDays(366);

        assertThatThrownBy(() -> new ReportFilter(from, to)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("toExclusive() deve ser meia-noite do dia seguinte ao ultimo dia, garantindo inclusividade")
    void toExclusiveIsMidnightOfFollowingDay() {
        ReportFilter filter = new ReportFilter(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertThat(filter.toExclusive()).isEqualTo(LocalDate.of(2026, 2, 1).atStartOfDay());
        assertThat(filter.fromInclusive()).isEqualTo(LocalDate.of(2026, 1, 1).atStartOfDay());
    }
}
