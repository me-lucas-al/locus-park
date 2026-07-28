package com.locuspark.api.controller;

import com.locuspark.api.dto.request.ClientRequest;
import com.locuspark.api.dto.response.ClientResponse;
import com.locuspark.api.entity.User;
import com.locuspark.api.enums.UserRole;
import com.locuspark.api.exception.BusinessException;
import com.locuspark.api.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientResponse> create(
            @RequestBody @Valid ClientRequest request,
            @AuthenticationPrincipal User user) {
        validateUserAndCompany(user);
        ClientResponse response = clientService.createClient(user.getCompany().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> getAll(@AuthenticationPrincipal User user) {
        validateUserAndCompany(user);
        List<ClientResponse> clients = clientService.listAllClientsByCompany(user.getCompany().getId());
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        validateUserAndCompany(user);
        ClientResponse response = clientService.getClientByIdAndCompany(id, user.getCompany().getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> update(
            @PathVariable UUID id,
            @RequestBody @Valid ClientRequest request,
            @AuthenticationPrincipal User user) {
        validateUserAndCompany(user);
        ClientResponse response = clientService.updateClient(id, user.getCompany().getId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        validateUserAndCompany(user);

        if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.SUPER_ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        clientService.deleteClient(id, user.getCompany().getId());
        return ResponseEntity.noContent().build();
    }

    private void validateUserAndCompany(User user) {
        if (user == null || (user.getRole() != UserRole.SUPER_ADMIN && user.getCompany() == null)) {
            throw new BusinessException("Acesso negado: Usuário não vinculado a uma empresa.");
        }
    }
}
