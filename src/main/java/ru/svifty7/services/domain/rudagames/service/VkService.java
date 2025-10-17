package ru.svifty7.services.domain.rudagames.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.Keyboard;
import com.vk.api.sdk.objects.photos.responses.GetMessagesUploadServerResponse;
import com.vk.api.sdk.objects.photos.responses.PhotoUploadResponse;
import com.vk.api.sdk.objects.photos.responses.SaveMessagesPhotoResponse;
import com.vk.api.sdk.objects.users.Fields;
import com.vk.api.sdk.objects.users.GetNameCase;
import com.vk.api.sdk.objects.users.responses.GetResponse;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.svifty7.services.domain.rudagames.dto.VkUserInfo;
import ru.svifty7.services.domain.rudagames.listener.VkLongPollHandler;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class VkService {

    private final Random random = new Random();
    @Getter
    private final VkApiClient vkApiClient;
    @Getter
    private final GroupActor groupActor;
    @Getter
    private final Integer peerId;
    private final VkLongPollHandler longPollHandler;
    private final Gson gson = new Gson();
    private final ScheduledExecutorService scheduler;


    public VkService(
            @Value("${vk.group.id}") int groupId,
            @Value("${vk.group.token}") String accessToken,
            @Value("${rudagames.vk.team-chat-id}") int peerId,
            @Lazy VkLongPollHandler longPollHandler) {
        TransportClient transportClient = new HttpTransportClient();
        this.vkApiClient = new VkApiClient(transportClient);
        this.groupActor = new GroupActor(groupId, accessToken);
        this.peerId = peerId;
        this.longPollHandler = longPollHandler;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @EventListener(ApplicationStartedEvent.class)
    public void startLongPoll() {
        runLongPoll();
    }

    private void runLongPoll() {
        try {
            log.info("starting VK long poll...");
            checkLongPollAvailability();
            longPollHandler.run();
        } catch (Exception e) {
            log.error("long poll stopped: {}, restarting in 5 sec...", e.getMessage(), e);
            scheduler.schedule(this::runLongPoll, 5, TimeUnit.SECONDS);
        }
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void checkLongPollAvailability() throws ClientException, ApiException {
        vkApiClient.groups()
                .getLongPollServer(groupActor, groupActor.getGroupId())
                .execute();
        log.info("long poll server is available");
    }

    public void sendMessage(String text) throws ClientException, ApiException {
        vkApiClient.messages()
                .send(groupActor)
                .peerId(peerId)
                .message(text)
                .disableMentions(true)
                .randomId(random.nextInt())
                .execute();
    }

    public void sendMessageWithPhoto(String markdownText, String imageUrl)
            throws ClientException, ApiException, IOException {
        sendMessageWithPhotoAndKeyboard(markdownText, imageUrl, null);
    }

    public Integer sendMessageWithPhotoAndKeyboard(String markdownText, String imageUrl, Keyboard keyboard)
            throws ClientException, ApiException, IOException {
        File tempFile = Files.createTempFile("vk_img_", ".jpg").toFile();
        try {
            FileUtils.copyURLToFile(URI.create(imageUrl).toURL(), tempFile);

            SaveMessagesPhotoResponse photo = uploadPhoto(tempFile);
            String attachment = String.format("photo%s_%s", photo.getOwnerId(), photo.getId());

            var query = vkApiClient.messages()
                    .send(groupActor)
                    .unsafeParam("peer_ids", String.valueOf(peerId))
                    .message(markdownText)
                    .attachment(attachment)
                    .disableMentions(true)
                    .randomId(random.nextInt());

            if (keyboard != null) {
                query.keyboard(keyboard);
            }

            String response = query.executeAsString();

            JsonArray responseArray = JsonParser.parseString(response).getAsJsonObject()
                    .getAsJsonArray("response");
            JsonObject messageData = responseArray.get(0).getAsJsonObject();

            return messageData.get("conversation_message_id").getAsInt();
        } finally {
            if (!tempFile.delete()) {
                log.warn("failed to delete temp file: {}", tempFile.getAbsolutePath());
            }
        }
    }

    private SaveMessagesPhotoResponse uploadPhoto(File file)
            throws ClientException, ApiException {
        GetMessagesUploadServerResponse uploadSrv = vkApiClient.photos()
                .getMessagesUploadServer(groupActor)
                .execute();

        PhotoUploadResponse upload = vkApiClient.upload()
                .photo(uploadSrv.getUploadUrl().toString(), file)
                .execute();

        if (upload.getPhoto() == null || upload.getPhoto().length() < 3) {
            throw new IllegalStateException("upload.photo is empty");
        }

        return vkApiClient.photos()
                .saveMessagesPhoto(groupActor, upload.getPhoto())
                .server(upload.getServer())
                .hash(upload.getHash())
                .execute()
                .getFirst();
    }

    public void sendCallbackAnswerToSnackbar(String eventId, Integer userId, Integer peerId, String text)
            throws ClientException, ApiException {
        Map<String, String> eventData = new HashMap<>();
        eventData.put("type", "show_snackbar");
        eventData.put("text", text);

        vkApiClient.messages()
                .sendMessageEventAnswer(groupActor, eventId, userId, peerId)
                .eventData(gson.toJson(eventData))
                .execute();
    }

    public void sendCallbackEmptyAnswerToSnackbar(String eventId, Integer userId, Integer peerId)
            throws ClientException, ApiException {
        vkApiClient.messages()
                .sendMessageEventAnswer(groupActor, eventId, userId, peerId)
                .execute();
    }

    public GetResponse getUserById(int userId) throws ClientException, ApiException {
        return getUserById(userId, GetNameCase.NOMINATIVE);
    }

    public GetResponse getUserById(int userId, GetNameCase nameCase) throws ClientException, ApiException {
        return vkApiClient.users()
                .get(groupActor)
                .userIds(String.valueOf(userId))
                .fields(Fields.SEX, Fields.PHOTO_200)
                .nameCase(nameCase)
                .execute()
                .getFirst();
    }

    public String createMention(VkUserInfo user) {
        return createMention(user, GetNameCase.NOMINATIVE);
    }

    public String createMention(VkUserInfo user, GetNameCase nameCase) {
        String displayName = user.getFullName(nameCase);

        return String.format("[id%d|%s]", user.id(), displayName);
    }
}
