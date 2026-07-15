package com.locuspark.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record RegisterRequest(
        @NotBlank(message = "O nome de usuário é obrigatório")
        @Size(min = 3, max = 20, message = "O nome de usuário deve ter entre 3 e 20 caracteres")
        String username,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String password,

        UUID companyId,

        String companyName,
        String cnpj,
        Integer totalSpots
) {
    public RegisterRequest(String username, String password, UUID companyId) {
        this(username, password, companyId, null, null, null);
    }
}