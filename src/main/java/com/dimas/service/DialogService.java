package com.dimas.service;

import com.dimas.api.model.ApiDialogMessage;
import com.dimas.domain.entity.DialogMessage;
import com.dimas.domain.mapper.DialogMessageMapper;
import com.dimas.persistence.DialogRepository;
import com.dimas.util.HashUtil;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class DialogService {

    //По идее тут надо вебкоет публикацию делать? или возможно это потом при декомпозиции на микросервисы добавить а сейчас типа акцент на шардировании
    private final DialogRepository dialogRepository;
    private final DialogMessageMapper dialogMessageMapper;
    private final AuthenticationContext context;

    public Uni<List<ApiDialogMessage>> getById(UUID toUser) {
        log.debug("currentUser={}", context.getCurrentUser());
        var userId = context.getCurrentUser().getId();
        return dialogRepository.dialog(userId, toUser)
                .map(list -> list.stream().map(dialogMessageMapper::map).toList());
    }

    public Uni<Void> sendDialogMessage(UUID toUser, String text) {
        log.debug("currentUser={}", context.getCurrentUser());
        var userId = context.getCurrentUser().getId();
        return dialogRepository.create(DialogMessage.builder()
                        .dialogId(HashUtil.getDialogHash(userId, toUser))
                        .fromUser(userId)
                        .toUser(toUser)
                        .text(text)
                        .build())
                .invoke(msg -> log.debug("DialogMessage successfully saved={}", msg))
                .replaceWithVoid();
    }

}
