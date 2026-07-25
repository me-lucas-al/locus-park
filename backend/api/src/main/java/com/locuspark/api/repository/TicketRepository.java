package com.locuspark.api.repository;

import com.locuspark.api.entity.Ticket;
import com.locuspark.api.enums.TicketStatus;
import com.locuspark.api.types.Plate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID>{
    boolean existsByVehiclePlateAndCompanyIdAndStatus(Plate plate, UUID companyId, TicketStatus status);
    long countByCompanyIdAndStatus(UUID companyId, TicketStatus status);
    Optional<Ticket> findByIdAndCompanyId(UUID id, UUID companyId);
    List<Ticket> findAllByCompanyIdAndStatus(UUID companyId, TicketStatus status);
    List<Ticket> findAllByCompanyId(UUID companyId);
}