package com.hitsuthar.junescrapper.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class CacheConfig {
    @Bean
    fun cacheManager(): CacheManager {
        val cacheManager = CaffeineCacheManager("movieDrive","UHD","OD","MoviesMod","RogMovies")
        cacheManager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.DAYS).maximumSize(1000))
        return cacheManager
    }
}