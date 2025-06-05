package com.chqiuu.browser.robot.server.controller;

import com.chqiuu.browser.robot.server.handler.BrowserControlWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommandController {

    private final BrowserControlWebSocketHandler browserControlWebSocketHandler;

    @Autowired
    public CommandController(BrowserControlWebSocketHandler browserControlWebSocketHandler) {
        this.browserControlWebSocketHandler = browserControlWebSocketHandler;
    }

    /**
     * 调用此HTTP接口，向浏览器插件发送打开Baidu并搜索的指令。
     * 例如：访问 http://localhost:8080/triggerSearch?query=哪里有好吃的
     * @param query 搜索关键词
     * @return 状态信息
     */
    @GetMapping("/triggerSearch")
    public String triggerBaiduSearch(@RequestParam String query) {
        browserControlWebSocketHandler.sendCommandToBrowser("openBaiduAndSearch", query);
        return "已向浏览器插件发送指令：打开Baidu并搜索 '" + query + "'";
    }
}