package com.locuspark.api.controller;

import com.locuspark.api.dto.request.CompanyRequest;
import com.locuspark.api.dto.response.CompanyResponse;
import com.locuspark.api.entity.Company;
import com.locuspark.api.service.CompanyService;
import com.locuspark.api.repository.CompanyRepository;
import com.locuspark.api.exception.BusinessException;
import com.locuspark.api.dto.request.AuthRequest;
import com.locuspark.api.dto.request.RegisterRequest;
import com.locuspark.api.dto.response.AuthResponse;
import com.locuspark.api.entity.User;
import com.locuspark.api.enums.UserRole;
import com.locuspark.api.exception.InvalidCredentialsException;
import com.locuspark.api.exception.UserAlreadyExistsException;
import com.locuspark.api.repository.UserRepository;
import com.locuspark.api.security.TokenService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder; 
    private final CompanyService companyService;
    private final CompanyRepository companyRepository;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest data) {
        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(
                    data.username(),
                    data.password()
            );
            var auth = authenticationManager.authenticate(usernamePassword);

            User user = (User) auth.getPrincipal();

            var token = tokenService.generateToken((User) auth.getPrincipal());

            var companyId = user.getCompany() != null ? user.getCompany().getId() : null;

            return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getUsername(), user.getRole(), companyId));

        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Usuário ou senha incorretos");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequest data) {

        if (repository.findByUsername(data.username()) != null) {
            throw new UserAlreadyExistsException("O usuário '" + data.username() + "' já existe.");
        }

        String encryptedPassword = passwordEncoder.encode(data.password());
        User newUser;

        // Se foram passados dados de criação de empresa
        if (data.companyName() != null && !data.companyName().isBlank() && data.cnpj() != null && !data.cnpj().isBlank()) {
            CompanyResponse companyResponse = companyService.createCompany(
                new CompanyRequest(data.companyName(), data.cnpj(), data.totalSpots() != null ? data.totalSpots() : 0)
            );
            Company company = companyRepository.findById(companyResponse.id())
                .orElseThrow(() -> new BusinessException("Empresa criada não encontrada."));

            newUser = new User(data.username(), encryptedPassword, UserRole.ADMIN, company);
        } else {
            // Caso contrário, tenta associar pela companyId se fornecida
            Company company = null;
            if (data.companyId() != null) {
                company = companyRepository.findById(data.companyId())
                    .orElseThrow(() -> new BusinessException("Empresa não encontrada com o ID fornecido."));
            }
            newUser = new User(data.username(), encryptedPassword, UserRole.EMPLOYEE, company);
        }

        repository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}