package com.example.webgitclone.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
public class GitCloneValidationController {

    @PostMapping("/clone-check")
    public ResponseEntity<String> gitClonePage(
            @RequestParam(value = "branch", required = false) String branch,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "targetDir", required = false)String targetDir) {
        if (StringUtils.isEmpty(branch)) {
            return ResponseEntity.badRequest().body("브랜치 명을 입력해주세요.");
        }

        if (file == null) {
            return ResponseEntity.badRequest().body("파일을 업로드해주세요.");
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("파일이 비어있습니다.");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".txt")) {
            return ResponseEntity.badRequest().body("txt 파일만 업로드 가능합니다.");
        }

        if (StringUtils.isEmpty(targetDir)) {
            return ResponseEntity.badRequest().body("클론할 경로를 입력하세요.");
        }

        if (targetDir.startsWith("~")) {
            return ResponseEntity.badRequest().body("~로 시작하는 경로는 입력할 수 없습니다.");
        }

        File dir = new File(targetDir);
        if (!dir.exists() || !dir.isDirectory()) {
            return ResponseEntity.badRequest().body("입력한 경로가 존재하지 않습니다.");
        }

        // 현재 실행 경로에 업로드 폴더 생성 후 저장
        try {
            String basePath = new File(".").getCanonicalPath();  // 현재 실행 위치
            String uploadDir = basePath + "/uploaded/";
            Files.createDirectories(Paths.get(uploadDir)); // 디렉토리 없으면 생성

            String originalfileName = file.getOriginalFilename();
            File targetFile = new File(uploadDir + originalfileName);
            file.transferTo(targetFile);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("파일 저장 실패: " + e.getMessage());
        }

        return ResponseEntity.ok("");
    }
}
