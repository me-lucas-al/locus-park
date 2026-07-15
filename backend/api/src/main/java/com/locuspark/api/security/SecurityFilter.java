package com.locuspark.api.security;

import com.locuspark.api.entity.User;
import com.locuspark.api.exception.TokenMissingException;
import com.locuspark.api.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepository repository;
    private final HandlerExceptionResolver exceptionResolver;

    @Autowired
    public SecurityFilter(TokenService tokenService,
                          UserRepository repository,
                          @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        this.tokenService = tokenService;
        this.repository = repository;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            String origin = request.getHeader("Origin");
            if ("http://localhost:4200".equals(origin) || "https://locus-park.vercel.app".equals(origin)) {
                response.setHeader("Access-Control-Allow-Origin", origin);
            } else {
                response.setHeader("Access-Control-Allow-Origin", "https://locus-park.vercel.app");
            }
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
            response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Cache-Control");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        try {
            var token = this.recoverToken(request);
            String path = request.getServletPath();
            if (path == null || path.isEmpty()) {
                path = request.getRequestURI();
            }

            boolean isPublic = path.equals("/auth/login") || path.equals("/auth/register") ||
                    path.equals("/api/hello") ||
                    path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui") ||
                    path.startsWith("/swagger-resources") || path.startsWith("/webjars");

            if (token == null && !isPublic) {
                throw new TokenMissingException("Token não fornecido ou cabeçalho Authorization ausente.");
            }

            if (token != null) {
                var username = tokenService.validateToken(token); // O username é pego aqui
                UserDetails user = repository.findByUsername(username); // O findByUsername roda aqui

                if (user != null) {
                    var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    if (user instanceof User appUser && appUser.getCompany() != null) {
                        request.setAttribute("companyId", appUser.getCompany().getId());
                    }
                }
            }

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            exceptionResolver.resolveException(request, response, null, ex);
        }
    }
    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }
}