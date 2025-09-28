package ru.svifty7.services.domain.rudagames.service;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.photos.responses.GetMessagesUploadServerResponse;
import com.vk.api.sdk.objects.photos.responses.PhotoUploadResponse;
import com.vk.api.sdk.objects.photos.responses.SaveMessagesPhotoResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Random;

@Slf4j
@Service
public class VkService {

    private final Random random = new Random();
    private final VkApiClient vkApiClient;
    private final GroupActor groupActor;
    private final Integer peerId;

    public VkService(
            @Value("${vk.group.id}") int groupId,
            @Value("${vk.group.token}") String accessToken,
            @Value("${rudagames.vk.team-chat-id}") int peerId
    ) {
        TransportClient transportClient = new HttpTransportClient();
        this.vkApiClient = new VkApiClient(transportClient);
        this.groupActor = new GroupActor(groupId, accessToken);
        this.peerId = peerId;
    }

    public void sendMessageWithPhoto(String markdownText, String imageUrl) throws ClientException, ApiException, IOException {
        File tempFile = Files.createTempFile("vk_img_", ".jpg").toFile();

        try {
            FileUtils.copyURLToFile(URI.create(imageUrl).toURL(), tempFile);

            GetMessagesUploadServerResponse uploadSrv = vkApiClient.photos()
                    .getMessagesUploadServer(groupActor)
                    .execute();

            PhotoUploadResponse upload = vkApiClient.upload()
                    .photo(uploadSrv.getUploadUrl().toString(), tempFile)
                    .execute();

            if (upload.getPhoto() == null || upload.getPhoto().length() < 3) {
                throw new IllegalStateException("upload.photo is empty");
            }

            SaveMessagesPhotoResponse saved = vkApiClient.photos()
                    .saveMessagesPhoto(groupActor, upload.getPhoto())
                    .server(upload.getServer())
                    .hash(upload.getHash())
                    .execute()
                    .getFirst();

            String attachment = String.format("photo%s_%s", saved.getOwnerId(), saved.getId());

            vkApiClient.messages()
                    .send(groupActor)
                    .peerId(peerId)
                    .message(markdownText)
                    .attachment(attachment)
                    .randomId(new Random().nextInt())
                    .execute();

        } finally {
            tempFile.delete();
        }
    }

    public void sendMessage(String messageText) throws ClientException, ApiException {
        vkApiClient.messages()
                .send(groupActor)
                .peerId(peerId)
                .message(messageText)
                .randomId(random.nextInt())
                .execute();
    }
}
