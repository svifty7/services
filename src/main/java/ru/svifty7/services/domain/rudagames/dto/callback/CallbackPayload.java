package ru.svifty7.services.domain.rudagames.dto.callback;

public sealed interface CallbackPayload permits AcceptedGameInvitePayload, TeamRegistrationPayload {
    String type();
}
