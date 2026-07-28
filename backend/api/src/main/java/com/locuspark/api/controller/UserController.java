package com.locuspark.api.controller;

import com.locuspark.api.dto.request.RegisterRequest;
import com.locuspark.api.dto.request.RoleUpdateRequest;
import com.locuspark.api.dto.request.UserUpdateRequest;
import com.locuspark.api.dto.response.UserResponse;
import com.locuspark.api.entity.User;
import com.locuspark.api.exception.BusinessException;
import com.locuspark.api.mapper.UserMapper;
import com.locuspark.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal User user) {
        if (user == null) {
            throw new BusinessException("Usuário não encontrado no sistema.");
        }
        // Retorna o DTO completo, incluindo o companyId mapeado pelo MapStruct
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @PostMapping("/company/{companyId}")
    public ResponseEntity<UserResponse> createNewCollaborator(
            @PathVariable UUID companyId,
            @RequestBody @Valid RegisterRequest request) {

        RegisterRequest localizedRequest = new RegisterRequest(request.username(), request.password(), companyId);

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(localizedRequest));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<UserResponse>> getByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(userService.listAllByCompany(companyId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable UUID id,
            @RequestBody @Valid UserUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.updateUser(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        userService.deleteUser(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateRole(
            @PathVariable UUID id,
            @RequestBody @Valid RoleUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userService.updateUserRole(id, request, currentUser));
    }
}