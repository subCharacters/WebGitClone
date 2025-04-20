package com.example.webgitclone.config;

import com.example.webgitclone.controller.handler.GitCloneLogHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final GitCloneLogHandler gitCloneLogHandler;

    public WebSocketConfig(GitCloneLogHandler gitCloneLogHandler) {
        this.gitCloneLogHandler = gitCloneLogHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // /ws/clone-logs 경로로 들어오는 WebSocket 요청을 GitCloneLogHandler로 처리
        // setAllowedOrigins("*") 는 CORS 제한 없이 접속 허용
        registry.addHandler(gitCloneLogHandler, "/ws/clone-logs").setAllowedOrigins("*");
    }
}
