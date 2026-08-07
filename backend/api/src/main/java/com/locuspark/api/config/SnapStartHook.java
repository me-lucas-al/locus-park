package com.locuspark.api.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import org.crac.Context;
import org.crac.Core;
import org.crac.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SnapStartHook implements Resource {

    private static final Logger log = LoggerFactory.getLogger(SnapStartHook.class);

    @Autowired
    private HikariDataSource dataSource;

    @PostConstruct
    public void register() {
        Core.getGlobalContext().register(this);
    }

    @Override
    public void beforeCheckpoint(Context<? extends Resource> context) throws Exception {
        // Evicta conexões antigas sem fechar o pool
        dataSource.getHikariPoolMXBean().softEvictConnections();
    }

    @Override
    public void afterRestore(Context<? extends Resource> context) throws Exception {
        // Não propaga falha: se o banco estiver indisponível no restore, o pool
        // tenta de novo de forma lazy na primeira requisição real, que responde
        // via Spring (com CORS) em vez de derrubar o restore do Lambda inteiro.
        try {
            dataSource.getHikariPoolMXBean().softEvictConnections();
            try (var conn = dataSource.getConnection()) {
                conn.isValid(5);
            }
        } catch (Exception e) {
            log.warn("Falha ao validar conexão com o banco durante afterRestore do SnapStart", e);
        }
    }
}