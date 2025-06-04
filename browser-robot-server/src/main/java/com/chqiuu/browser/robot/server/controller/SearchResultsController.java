package com.chqiuu.browser.robot.server.controller;


import com.chqiuu.browser.robot.server.handler.CommandWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// SearchResultsController.java (接收搜索结果)
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SearchResultsController {
    // 示例：通过一个REST接口触发发送命令（模拟管理界面）
    private final CommandWebSocketHandler commandHandler;
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
        commandHandler.sendCommandToAllPlugins(command);
        return ResponseEntity.ok("Command sent to plugins!");
    }
}