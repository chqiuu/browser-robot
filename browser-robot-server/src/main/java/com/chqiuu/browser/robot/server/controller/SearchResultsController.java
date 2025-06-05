package com.chqiuu.browser.robot.server.controller;


import com.chqiuu.browser.robot.server.common.constant.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;

// SearchResultsController.java (接收搜索结果)
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SearchResultsController {

    @PostMapping("/search-results")
    public ResponseEntity<String> receiveSearchResults(@RequestBody List<Map<String, String>> searchResults) {
        System.out.println("Received search results from browser extension:");
        searchResults.forEach(result -> System.out.println("  " + result.get("title") + " - " + result.get("link")));
        // 在这里可以进一步处理这些结果，例如保存到数据库
        return ResponseEntity.ok("Results received successfully!");
    }


    @GetMapping("/trigger-search-command")
    public ResponseEntity<String> triggerSearchCommand() {
        String command = "{\"command\": \"perform_baidu_search\"}";
        sendCommandToAllPlugins(command);
        return ResponseEntity.ok("Command sent to plugins!");
    }


    // 后端发送指令给所有连接的插件
    public void sendCommandToAllPlugins(String commandJson) {
        for (WebSocketSession session :  Constant.SOCKET_SESSIONS) {
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