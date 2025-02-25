package com.fourseason.delivery.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);  // 기본 스레드 풀 크기
        executor.setMaxPoolSize(10);  // 최대 스레드 수
        executor.setQueueCapacity(25);  // 작업 대기 큐 크기
        executor.setThreadNamePrefix("async-exec-");
        executor.initialize();
        return executor;
    }
}