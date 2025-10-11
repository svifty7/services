package ru.svifty7.services.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Primary
    @Bean("vkCacheManager")
    public CacheManager vkCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("vkUsers");
        manager.setCaffeine(
                Caffeine.newBuilder()
                        .initialCapacity(50)
                        .maximumSize(200)
                        .expireAfterWrite(30L, TimeUnit.DAYS)
                        .recordStats()
        );
        return manager;
    }

    @Bean("eventsCacheManager")
    public CacheManager eventsCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("events");
        manager.setCaffeine(
                Caffeine.newBuilder()
                        .initialCapacity(100)
                        .maximumSize(500)
                        .expireAfterWrite(1L, TimeUnit.HOURS)
                        .recordStats()
        );
        return manager;
    }

    @Bean("teamsCacheManager")
    public CacheManager teamsCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("teams");
        manager.setCaffeine(
                Caffeine.newBuilder()
                        .initialCapacity(20)
                        .maximumSize(100)
                        .expireAfterWrite(7L, TimeUnit.DAYS)
                        .recordStats()
        );
        return manager;
    }
}
