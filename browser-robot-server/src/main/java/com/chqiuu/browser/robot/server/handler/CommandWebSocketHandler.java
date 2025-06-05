package com.chqiuu.browser.robot.server.handler;


import com.chqiuu.browser.robot.server.common.constant.Constant;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

// CommandWebSocketHandler.java
public class CommandWebSocketHandler extends TextWebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Constant.SOCKET_SESSIONS.add(session);
        System.out.println("WebSocket session established: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 接收来自插件的消息（如果插件需要发送消息给后端）
        System.out.println("Received from plugin: " + message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Constant.SOCKET_SESSIONS.remove(session);
        System.out.println("WebSocket session closed: " + session.getId());
    }

}