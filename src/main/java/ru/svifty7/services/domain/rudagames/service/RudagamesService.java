package ru.svifty7.services.domain.rudagames.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;
import ru.svifty7.services.domain.rudagames.config.RudagamesConfig;
import ru.svifty7.services.domain.rudagames.dto.AcceptedGame;
import ru.svifty7.services.domain.rudagames.dto.CityEvent;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RudagamesService {

    private final AuthService authService;
    private final ObjectMapper objectMapper;
    private final RudagamesConfig rudagamesConfig;

    private final OkHttpClient okHttpClient = new OkHttpClient();

    public List<CityEvent> getEvents() throws IOException {
        Request request = new Request.Builder()
                .url(rudagamesConfig.getEventsUrl())
                .header("Authorization", "Bearer " + authService.getToken())
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

    public List<AcceptedGame> getAcceptedGames() throws IOException {
        Request request = new Request.Builder()
                .url(rudagamesConfig.getAcceptedGamesUrl())
                .header("Authorization", "Bearer " + authService.getToken())
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
