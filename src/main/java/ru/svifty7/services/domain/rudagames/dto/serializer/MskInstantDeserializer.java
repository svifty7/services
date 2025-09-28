package ru.svifty7.services.domain.rudagames.dto.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
* Десериализатор для дат в MSK (Europe/Moscow) в Instant.
**/
public class MskInstantDeserializer extends JsonDeserializer<Instant> {

    private static final ZoneId MSK_ZONE = ZoneId.of("Europe/Moscow");

    @Override
    public Instant deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String dateString = jsonParser.getText();
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        LocalDateTime localDateTime = LocalDateTime.parse(dateString);
        ZonedDateTime zonedDateTime = localDateTime.atZone(MSK_ZONE);
        return zonedDateTime.toInstant();
    }

}
