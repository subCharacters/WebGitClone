package com.example.webgitclone.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class GitCloneService {

    public void gitClone(String srcPath, String destPath) {

        String branch = "master";
        String authUrl = "https://github.com/subCharacters/DesignPattern_Java.git";
        String targetPath = "C:\\work\\test\\";
        String targetDir = authUrl.substring(authUrl.lastIndexOf("/") + 1).replace(".git", "");

        // Git 명령어 구성: git clone -b 브랜치명 URL 대상경로
        ProcessBuilder processBuilder = new ProcessBuilder(
                "git", "clone", "-b", branch, authUrl, targetPath + targetDir
        );

        // 표준 에러 스트림(stderr)을 표준 출력(stdout)에 합치기
        // -> 모든 출력(성공/실패 메시지)을 하나의 스트림으로 읽기 위함
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[GIT] " + line); // Git의 출력 내용을 콘솔에 표시
                }
            }
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.err.println("ExitCode: " + exitCode);
                System.out.println("Clone Failed");
                return;
            }

            System.out.println("Clone Successful");
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
