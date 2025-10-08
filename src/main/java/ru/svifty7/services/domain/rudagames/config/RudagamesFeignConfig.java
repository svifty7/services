package ru.svifty7.services.domain.rudagames.config;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.svifty7.services.domain.rudagames.service.AuthService;

@Configuration
@RequiredArgsConstructor
public class RudagamesFeignConfig {
    private final AuthService authService;

    @Bean
    public RequestInterceptor bearerTokenInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Authorization", "Bearer " + authService.getToken());
        };
    }
}
