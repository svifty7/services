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
    private final VkApiClient vk;
    private final GroupActor actor;
    private final Integer peerId;

    public VkService(
            @Value("${vk.peer-id}") int peerId,
            @Value("${vk.group-id}") int groupId,
            @Value("${vk.access-token}") String accessToken) {
        TransportClient transportClient = new HttpTransportClient();
        this.vk = new VkApiClient(transportClient);
        this.actor = new GroupActor(groupId, accessToken);
        this.peerId = peerId;
    }

    public void sendMessageWithPhoto(String markdownText, String imageUrl) throws ClientException, ApiException, IOException {
        File tempFile = Files.createTempFile("vk_img_", ".jpg").toFile();
        try {
            FileUtils.copyURLToFile(URI.create(imageUrl).toURL(), tempFile);

            GetMessagesUploadServerResponse uploadSrv = vk.photos()
                    .getMessagesUploadServer(actor)
                    .peerId(peerId)
                    .execute();

            PhotoUploadResponse upload = vk.upload()
                    .photo(uploadSrv.getUploadUrl().toString(), tempFile)
                    .execute();

            if (upload.getPhoto() == null || upload.getPhoto().length() < 3)
                throw new IllegalStateException("upload.photo is empty");

            SaveMessagesPhotoResponse saved = vk.photos()
                    .saveMessagesPhoto(actor, upload.getPhoto())
                    .server(upload.getServer())
                    .hash(upload.getHash())
                    .execute()
                    .getFirst();

            String attachment = "photo" + saved.getOwnerId() + '_' + saved.getId();

            vk.messages()
                    .send(actor)
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
        vk.messages()
                .send(actor)
                .peerId(peerId)
                .message(messageText)
                .randomId(random.nextInt())
                .execute();
    }
}
