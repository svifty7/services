package ru.svifty7.services.domain.rudagames.config;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.svifty7.services.domain.rudagames.dto.AuthResponse;
import ru.svifty7.services.domain.rudagames.feign.AuthFeign;

@Configuration
@RequiredArgsConstructor
public class RudagamesFeignConfig {
    private final AuthPayload authPayload;
    private final AuthFeign authFeign;

    @Bean
    public RequestInterceptor bearerTokenInterceptor() {
        return requestTemplate -> {
            AuthResponse authResponse = authFeign.signIn(authPayload);
            String token = authResponse.accessToken();
            requestTemplate.header("Authorization", "Bearer " + token);
        };
    }
}
