package com.dimas.controller;

import com.dimas.controller.filter.Secured;
import com.dimas.service.AuthenticationContext;
import com.dimas.service.WebsocketService;
import io.smallrye.common.vertx.VertxContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import static com.dimas.util.Const.VERTEX_CONTEXT_SESSION_ID;
import static com.dimas.util.Const.WEBSOCKET_CONTEXT_USER;


@Slf4j
@ServerEndpoint(value = "/post/feed/posted")
@ApplicationScoped
@RequiredArgsConstructor
@Secured
public class WebsocketController {

    private final WebsocketService websocketService;
    private final AuthenticationContext context;

    @OnOpen
    public void onOpen(Session session) {
        log.debug("onOpen");
        var userId = VertxContext.getOrCreateDuplicatedContext().getLocal(VERTEX_CONTEXT_SESSION_ID).toString();
        log.debug("userId from vertx context={}", userId);
        var user = context.getCurrentUser();
        log.debug("user from auth context={}", user);
        websocketService.addSession(UUID.fromString(userId), session);
    }

    @OnClose
    public void onClose(Session session) {
        log.debug("onClose");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.debug("onError", throwable);
    }

    @OnMessage
    public void onMessage(Session session, String event) {
//        ignore it
        log.debug("Unexpected incoming message={}", event);
    }

}

