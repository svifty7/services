package ru.svifty7.services.domain.rudagames.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.stereotype.Service;
import ru.svifty7.services.domain.rudagames.config.AuthPayload;
import ru.svifty7.services.domain.rudagames.config.RudagamesConfig;
import ru.svifty7.services.domain.rudagames.dto.AcceptedGame;
import ru.svifty7.services.domain.rudagames.dto.AuthResponse;
import ru.svifty7.services.domain.rudagames.dto.CityEvent;
import ru.svifty7.services.domain.rudagames.exception.AuthException;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RudagamesService {

    private final AuthPayload authPayload;
    private final ObjectMapper objectMapper;
    private final RudagamesConfig rudagamesConfig;

    private final OkHttpClient okHttpClient = new OkHttpClient();

    private volatile String cachedToken;
    private volatile long expiryTime;

    public synchronized String getToken() {
        if (cachedToken == null || System.currentTimeMillis() > expiryTime) {
            signIn();
        }

        return String.format("Bearer %s", cachedToken);
    }

    private void signIn() {
        RequestBody body;
        try {
            String jsonPayload = objectMapper.writeValueAsString(authPayload);
            body = RequestBody.create(jsonPayload, MediaType.get("application/json"));
        } catch (Exception e) {
            throw new AuthException("failed to serialize auth payload: " + e.getMessage());
        }

        Request request = new Request.Builder()
                .url(rudagamesConfig.getSignInUrl())
                .post(body)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new AuthException("auth failed with code " + response.code());
            }

            String responseBody = response.body().string();
            AuthResponse authResponse = objectMapper.readValue(responseBody, AuthResponse.class);

            if (authResponse != null) {
                cachedToken = authResponse.accessToken();
                expiryTime = System.currentTimeMillis() + (authResponse.expiresIn() * 1000L - 60000L);
            } else {
                throw new AuthException("response is null");
            }
        } catch (IOException e) {
            throw new AuthException("Auth request error: " + e.getMessage());
        }
    }

    public List<CityEvent> loadActualEvents() throws IOException {
        Request request = new Request.Builder()
                .url(rudagamesConfig.getEventsUrl())
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String json = response.body().string();
            JsonNode node = objectMapper.readTree(json);

            if (node.isArray()) {
                return objectMapper.readValue(json,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, CityEvent.class));
            } else {
                throw new IOException("Expected JSON array but got: " + node.getNodeType());
            }
        }
    }

    public List<AcceptedGame> loadCurrentAcceptedGames() throws IOException {
        Request request = new Request.Builder()
                .url(rudagamesConfig.getAcceptedGamesUrl())
                .header("Authorization", getToken())
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String json = response.body().string();
            JsonNode node = objectMapper.readTree(json);

            if (node.isArray()) {
                return objectMapper.readValue(json,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, AcceptedGame.class));
            } else {
                throw new IOException("Expected JSON array but got: " + node.getNodeType());
            }
        }
    }
}
