package com.chqiuu.browser.robot.server.handler;


import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// CommandWebSocketHandler.java
public class CommandWebSocketHandler extends TextWebSocketHandler {
    private static final List<WebSocketSession> sessions = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("WebSocket session established: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 接收来自插件的消息（如果插件需要发送消息给后端）
        System.out.println("Received from plugin: " + message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("WebSocket session closed: " + session.getId());
    }

    // 后端发送指令给所有连接的插件
    public void sendCommandToAllPlugins(String commandJson) {
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(commandJson));
                    System.out.println("Sent command to plugin: " + commandJson);
                }
            } catch (IOException e) {
                System.err.println("Failed to send command to plugin: " + e.getMessage());
            }
        }
    }
}