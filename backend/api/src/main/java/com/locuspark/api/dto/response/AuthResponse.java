package com.locuspark.api.dto.response;

import com.locuspark.api.enums.UserRole;

import java.util.UUID;

public record AuthResponse(String token, UUID id, String username, UserRole role, UUID companyId) {}