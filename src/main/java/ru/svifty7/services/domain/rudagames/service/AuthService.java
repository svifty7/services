package ru.svifty7.services.domain.rudagames.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.svifty7.services.domain.rudagames.config.AuthPayload;
import ru.svifty7.services.domain.rudagames.dto.AuthResponse;
import ru.svifty7.services.domain.rudagames.exception.AuthException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RestTemplate restTemplate;
    private final AuthPayload authPayload;

    @Value("${rudagames.api.base-url}")
    private String baseUrl;

    @Value("${rudagames.api.sign-in}")
    private String authUrl;

    private volatile String cachedToken;
    private volatile long expiryTime;

    public synchronized String getToken() {
        if (cachedToken == null || System.currentTimeMillis() > expiryTime) {
            signIn();
        }

        return cachedToken;
    }

    private void signIn() {
        AuthResponse response;

        try {
            response = restTemplate.postForObject(
                    String.format("%s%s", baseUrl, authUrl),
                    authPayload,
                    AuthResponse.class);
        } catch (Exception e) {
            throw new AuthException(e.getMessage());
        }

        if (response != null) {
            cachedToken = response.accessToken();
            expiryTime = System.currentTimeMillis() + (response.expiresIn() * 1000 - 60000);
        } else {
            throw new AuthException("response is null");
        }
    }
}
