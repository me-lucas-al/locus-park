package com.locuspark.api.repository;

public final class TicketRecordQuery {

    private TicketRecordQuery() {
    }

    public static final String SELECT = """
            SELECT new com.locuspark.api.service.report.TicketRecord(
                t.id, t.status, t.enteredAt, t.exitedAt, t.totalAmount, t.grossAmount, t.discountAmount, t.paymentMethod,
                v.plate, v.model, v.color, v.type,
                c.id, c.name, c.cpf,
                p.id, p.name, p.discountType, p.value)
            FROM Ticket t
            JOIN t.vehicle v
            LEFT JOIN v.client c
            LEFT JOIN t.partnership p
            """;

    public static final String EXIT_WINDOW = SELECT + """
            WHERE t.company.id = :companyId
              AND t.status = com.locuspark.api.enums.TicketStatus.PAID
              AND t.exitedAt >= :from
              AND t.exitedAt < :to
            """;

    public static final String ENTRY_WINDOW = SELECT + """
            WHERE t.company.id = :companyId
              AND t.enteredAt >= :from
              AND t.enteredAt < :to
            """;
}
