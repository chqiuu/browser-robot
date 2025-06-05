package com.chqiuu.browser.robot.server.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class BrowserControlWebSocketHandler extends TextWebSocketHandler {

    // 存储所有活跃的WebSocketSession，以便可以向所有连接的浏览器发送指令
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private final ObjectMapper objectMapper = new ObjectMapper(); // 用于JSON的序列化和反序列化

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("新的WebSocket连接已建立: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("从浏览器插件接收到消息: " + payload);

        try {
            JsonNode jsonNode = objectMapper.readTree(payload);

            String type = jsonNode.has("type") ? jsonNode.get("type").asText() : "unknown";

            switch (type) {
                case "browser_ready":
                    System.out.println("浏览器插件已连接并准备就绪。");
                    // 可以记录会话，或发送欢迎/初始化命令
                    break;
                case "search_results":
                    System.out.println("收到搜索结果:");
                    // 这里你可以进一步解析 results 节点，例如保存到数据库，或进行其他业务处理
                    JsonNode resultsNode = jsonNode.get("results");
                    if (resultsNode != null && resultsNode.isArray()) {
                        resultsNode.forEach(result -> {
                            System.out.println("  标题: " + result.get("title").asText());
                            System.out.println("  URL: " + result.get("url").asText());
                            System.out.println("  摘要: " + result.get("snippet").asText());
                            System.out.println("  ---");
                        });
                        // 实际应用中，你会将这些结果映射到Java对象，并进行处理
                        // List<SearchResult> results = objectMapper.convertValue(resultsNode, new TypeReference<List<SearchResult>>() {});
                        // yourService.processSearchResults(results);
                    }
                    break;
                case "error":
                    System.err.println("从浏览器插件收到错误: " + jsonNode.get("message").asText());
                    break;
                default:
                    System.out.println("收到未知类型的消息: " + payload);
            }

        } catch (Exception e) {
            System.err.println("处理来自浏览器消息时出错: " + e.getMessage());
            session.sendMessage(new TextMessage("{\"status\":\"error\", \"message\":\"后端处理消息失败: " + e.getMessage() + "\"}"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("WebSocket连接已关闭: " + session.getId() + ", 状态: " + status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket传输错误，会话: " + session.getId() + ", 错误: " + exception.getMessage());
        // 可以选择在这里移除会话，或进行其他错误恢复操作
    }

    /**
     * 向所有连接的浏览器插件发送控制指令。
     * @param command 指令名称，例如 "openBaiduAndSearch"
     * @param query 如果是搜索指令，则是搜索的关键词
     */
    public void sendCommandToBrowser(String command, String query) {
        if (sessions.isEmpty()) {
            System.out.println("没有浏览器插件连接到后端，无法发送指令。");
            return;
        }

        // 构建要发送的JSON消息
        String message = String.format("{\"command\":\"%s\", \"query\":\"%s\"}", command, query);
        TextMessage textMessage = new TextMessage(message);

        // 遍历所有连接的会话并发送指令
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                    System.out.println("已向浏览器发送指令: " + message);
                }
            } catch (IOException e) {
                System.err.println("发送消息到浏览器会话 " + session.getId() + " 时出错: " + e.getMessage());
            }
        }
    }
}