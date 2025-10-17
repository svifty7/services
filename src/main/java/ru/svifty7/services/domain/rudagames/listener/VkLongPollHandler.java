package ru.svifty7.services.domain.rudagames.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vk.api.sdk.events.longpoll.GroupLongPollApi;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.objects.callback.messages.CallbackMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.svifty7.services.domain.rudagames.dto.callback.CallbackPayload;
import ru.svifty7.services.domain.rudagames.dto.callback.AcceptedGameInvitePayload;
import ru.svifty7.services.domain.rudagames.dto.callback.TeamRegistrationPayload;
import ru.svifty7.services.domain.rudagames.service.AcceptedGamesService;
import ru.svifty7.services.domain.rudagames.service.VkService;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class VkLongPollHandler extends GroupLongPollApi {

    private final Set<String> processedEvents = ConcurrentHashMap.newKeySet();
    private final AcceptedGamesService acceptedGamesService;
    private final VkService vkService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public VkLongPollHandler(
            VkService vkService,
            AcceptedGamesService acceptedGamesService) {
        super(vkService.getVkApiClient(), vkService.getGroupActor(), 25);
        this.acceptedGamesService = acceptedGamesService;
        this.vkService = vkService;
    }

    @Override
    protected String parse(CallbackMessage message) {
        // Проверка на null type для защиты от падения
        if (message == null || message.getType() == null) {
            log.warn("ignore empty type message");
            return null;
        }

        try {
            JsonObject object = message.getObject();
            if (object != null && object.has("event_id") && object.has("conversation_message_id")) {
                return handleMessageEvent(object);
            }

            return super.parse(message);
        } catch (Exception e) {
            log.error("error while parsing message with type {}: {}", message.getType(), e.getMessage(), e);
            return "ok";
        }
    }

    private String handleMessageEvent(JsonObject object) {
        String eventId = null;
        Integer userId = null;
        Integer peerIdFromEvent = null;

        try {
            eventId = object.get("event_id").getAsString();

            if (!processedEvents.add(eventId)) {
                log.debug("event {} already processed, skipping", eventId);
                return "OK";
            }

            userId = object.get("user_id").getAsInt();
            peerIdFromEvent = object.get("peer_id").getAsInt();
            Integer conversationMessageId = object.get("conversation_message_id").getAsInt();

            handleMessageEvent(eventId, userId, peerIdFromEvent, conversationMessageId, object);

            vkService.sendCallbackEmptyAnswerToSnackbar(eventId, userId, peerIdFromEvent);

            return "OK";
        } catch (Exception e) {
            log.error("error processing button click: {}", e.getMessage(), e);
            return handleEventError(eventId, userId, peerIdFromEvent, e);
        } finally {
            scheduleEventCleanup(eventId);
        }
    }

    private String extractPayload(JsonObject object) {
        if (object.get("payload").isJsonObject()) {
            return object.get("payload").toString();
        } else {
            return object.get("payload").getAsString();
        }
    }

    private void handleMessageEvent(
            String eventId,
            Integer userId,
            Integer peerId,
            Integer messageId,
            JsonObject object
    ) throws JsonProcessingException, ClientException, ApiException {
        String payload = extractPayload(object);
        log.info("processing event: eventId={}, userId={}, payload={}", eventId, userId, payload);

        JsonObject payloadJson = JsonParser.parseString(payload).getAsJsonObject();
        String eventType = payloadJson.get("type").getAsString();

        CallbackPayload event = switch (eventType) {
            case "accepted-game-invite" ->
                    objectMapper.readValue(payload, AcceptedGameInvitePayload.class);
            case "team-registration" ->
                    objectMapper.readValue(payload, TeamRegistrationPayload.class);
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };

        switch (event) {
            case AcceptedGameInvitePayload invite -> acceptedGamesService.handleAcceptedGameInviteCallback(eventId, userId, peerId, messageId, invite);
            case TeamRegistrationPayload reg -> log.info("reg event: eventId={}, userId={}, peerId={}, messageId={} payload={}", eventId, userId, peerId, messageId, reg);
        }
    }

    private String handleEventError(String eventId, Integer userId, Integer peerIdFromEvent, Exception e) {
        if (eventId != null && userId != null && peerIdFromEvent != null) {
            try {
                vkService.sendCallbackAnswerToSnackbar(eventId, userId, peerIdFromEvent, "🤨 Хм... Похоже на какую-то ошибку...");
            } catch (Exception ex) {
                log.error("failed to send error response: {}", ex.getMessage());
            }
        }

        if (eventId != null) {
            scheduleEventCleanup(eventId);
        }

        return "OK";
    }

    private void scheduleEventCleanup(String eventId) {
        new Thread(() -> {
            try {
                Thread.sleep(60000);
                processedEvents.remove(eventId);
                log.debug("cleaned up event from cache: {}", eventId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
