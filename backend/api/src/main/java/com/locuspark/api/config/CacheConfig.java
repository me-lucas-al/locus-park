package com.locuspark.api.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    private static final int MAX_ENTRIES = 200;
    private static final long TTL_MINUTES = 30;

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                buildCache("tariffs"),
                buildCache("pricing"),
                buildCache("partnerships")
        ));
        return manager;
    }

    private CaffeineCache buildCache(String name) {
        return new CaffeineCache(name,
                Caffeine.newBuilder()
                        .maximumSize(MAX_ENTRIES)
                        .expireAfterWrite(TTL_MINUTES, TimeUnit.MINUTES)
                        .build());
    }
}