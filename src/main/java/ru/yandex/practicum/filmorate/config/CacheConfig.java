package ru.yandex.practicum.filmorate.config;

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

    @Bean
    public CacheManager cacheManager() {

        CaffeineCache popularFilms = new CaffeineCache("popularFilms",
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .expireAfterWrite(15, TimeUnit.SECONDS)
                        .build());

        CaffeineCache genres = new CaffeineCache("genres",
                Caffeine.newBuilder()
                        .maximumSize(10)
                        .build());

        CaffeineCache mpa = new CaffeineCache("mpa",
                Caffeine.newBuilder()
                        .maximumSize(10)
                        .build());

        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(popularFilms, genres, mpa));
        return manager;
    }
}