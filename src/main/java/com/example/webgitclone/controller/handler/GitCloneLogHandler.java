package com.example.webgitclone.controller.handler;

import com.example.webgitclone.dto.CloneRequestDto;
import com.example.webgitclone.dto.GitCloneResult;
import com.example.webgitclone.service.GitCloneService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GitCloneLogHandler extends TextWebSocketHandler {
    private final GitCloneService gitCloneService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String UPLOAD_DIR = System.getProperty("java.io.tmpdir") + "/git-clone-upload/";

    public GitCloneLogHandler(GitCloneService gitCloneService) {
        this.gitCloneService = gitCloneService;
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            // 1. WebSocket 메시지를 JSON으로 파싱 → DTO
            CloneRequestDto dto = objectMapper.readValue(message.getPayload(), CloneRequestDto.class);

            // 2. 현재 실행 디렉토리 기준 업로드 폴더 동적으로 계산
            String baseDir = new File(".").getCanonicalPath();
            String uploadPath = baseDir + "/uploaded/";
            Path txtPath = Paths.get(uploadPath + dto.getFileName());

            // 3. 파일 존재 확인
            if (!Files.exists(txtPath)) {
                session.sendMessage(new TextMessage("[EXCEPTION] 업로드된 파일이 존재하지 않습니다."));
                return;
            }

            // 4. Git URL 리스트 추출
            List<String> urls = Files.readAllLines(txtPath).stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .collect(Collectors.toList());

            if (urls.isEmpty()) {
                session.sendMessage(new TextMessage("[EXCEPTION] 파일에 내용이 존재하지 않습니다."));
                return;
            }

            // 5. clone 실행 및 실시간 로그 전송
            // List<GitCloneResult> results = gitCloneService.cloneMultiple(urls, dto.getBranch(), dto.getTargetDir());

            for (String url : urls) {
                GitCloneResult result = gitCloneService.cloneSingle(url, dto.getBranch(), dto.getTargetDir());
                // 6. 로그를 WebSocket으로 전송
                for (String logLine : result.getLogs()) {
                    session.sendMessage(new TextMessage(logLine));
                }
            }

            // 6. 로그를 WebSocket으로 전송
            /*for (GitCloneResult result : results) {
                for (String logLine : result.getLogs()) {
                    session.sendMessage(new TextMessage(logLine));
                }
            }*/

            // 완료 후 클라이언트에 알림 전송
            session.sendMessage(new TextMessage("클론 실행 완료"));
        } catch (Exception e) {
            try {
                // 예외 발생 시 클라이언트에 예외 메시지 전송
                session.sendMessage(new TextMessage("[EXCEPTION] " + e.getMessage()));
            } catch (Exception ignored) {
            }
        }
    }
}
