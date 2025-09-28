package ru.svifty7.services.domain.rudagames.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component
@ConfigurationProperties(prefix = "rudagames.auth")
public class AuthPayload {
    private String email;
    private String password;
}
