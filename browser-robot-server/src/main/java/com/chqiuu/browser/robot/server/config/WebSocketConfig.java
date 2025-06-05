package com.chqiuu.browser.robot.server.config;

import com.chqiuu.browser.robot.server.handler.BrowserControlWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

// WebSocketConfig.java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final BrowserControlWebSocketHandler browserControlWebSocketHandler;

    public WebSocketConfig(BrowserControlWebSocketHandler browserControlWebSocketHandler) {
        this.browserControlWebSocketHandler = browserControlWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册WebSocket处理器，指定端点路径
        registry.addHandler(browserControlWebSocketHandler, "/ws/browser-control")
                // **重要**：在生产环境中，请将此处的允许源限制为你的浏览器插件的ID，
                // 例如：.setAllowedOrigins("chrome-extension://your_extension_id")
                // 或具体的网站域名。为了开发方便，暂时设置为允许所有来源。
                .setAllowedOrigins("*");
    }
}
