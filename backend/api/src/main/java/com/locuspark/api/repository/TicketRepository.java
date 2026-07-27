package com.locuspark.api.repository;

import com.locuspark.api.entity.Ticket;
import com.locuspark.api.enums.TicketStatus;
import com.locuspark.api.service.report.TicketRecord;
import com.locuspark.api.types.Plate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID>{
    boolean existsByVehiclePlateAndCompanyIdAndStatus(Plate plate, UUID companyId, TicketStatus status);
    long countByCompanyIdAndStatus(UUID companyId, TicketStatus status);
    Optional<Ticket> findByIdAndCompanyId(UUID id, UUID companyId);
    List<Ticket> findAllByCompanyIdAndStatus(UUID companyId, TicketStatus status);
    List<Ticket> findAllByCompanyId(UUID companyId);

    @Query(TicketRecordQuery.EXIT_WINDOW)
    List<TicketRecord> findPaidRecordsByExitWindow(@Param("companyId") UUID companyId, @Param("from") Instant from, @Param("to") Instant to);

    @Query(TicketRecordQuery.ENTRY_WINDOW)
    List<TicketRecord> findRecordsByEntryWindow(@Param("companyId") UUID companyId, @Param("from") Instant from, @Param("to") Instant to);

    @Query("""
            SELECT COUNT(t) FROM Ticket t
            WHERE t.company.id = :companyId
              AND t.enteredAt < :from
              AND (t.exitedAt IS NULL OR t.exitedAt >= :from)
            """)
    long countPresentAt(@Param("companyId") UUID companyId, @Param("from") Instant from);
}