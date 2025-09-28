package ru.svifty7.services.domain.rudagames.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.svifty7.services.domain.rudagames.dto.Response;
import ru.svifty7.services.domain.rudagames.exception.NoEventsForNotifyException;
import ru.svifty7.services.domain.rudagames.exception.UpdateEventsException;

@Log4j2
@ControllerAdvice
public class Handler {

    @ExceptionHandler({NoEventsForNotifyException.class})
    public ResponseEntity<Response> handleNothingToBeDoneException(Exception e) {
        log.warn("{} {}", e.getClass(), e.getMessage());

        return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).build();
    }

    @ExceptionHandler({UpdateEventsException.class})
    public ResponseEntity<Response> handleUpdateEventsException(Exception e) {
        log.error("{} {}", e.getClass(), e.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Response(500, "Ошибка обновления предстоящих игр", e.getMessage()));
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleOtherExceptions(Exception e) {
        log.error("{} {}", e.getClass(), e.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Response(500, "Неизвестная ошибка", e.getMessage()));
    }

}
