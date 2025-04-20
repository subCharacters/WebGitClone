package com.example.webgitclone.service;

import com.example.webgitclone.dto.GitCloneResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GitCloneService {

    private final GitCloneExecutor gitCloneExecutor;

    public GitCloneService() {
        this.gitCloneExecutor = this::gitClone;
    }

    public GitCloneService(GitCloneExecutor gitCloneExecutor) {
        this.gitCloneExecutor = gitCloneExecutor;
    }

    public List<GitCloneResult> cloneMultiple(List<String> urls, String branch, String targetDir) {
        List<GitCloneResult> results = new ArrayList<>();

        for (String url : urls) {
            String repoName = url.substring(url.lastIndexOf("/") + 1).replace(".git", "");
            String destDir = targetDir + "/" + repoName;
            // GitCloneResult result = gitClone(url, branch, destDir, repoName);
            GitCloneResult result = gitCloneExecutor.execute(url, branch, destDir, repoName);
            results.add(result);
        }

        return results;
    }

    public GitCloneResult cloneSingle(String url, String branch, String targetDir) {
        String repoName = url.substring(url.lastIndexOf("/") + 1).replace(".git", "");
        String destDir = targetDir + "/" + repoName;
        GitCloneResult result = gitCloneExecutor.execute(url, branch, destDir, repoName);

        return result;
    }

    private GitCloneResult gitClone(String url, String branch, String destDir, String repoName) {
        List<String> logs = new ArrayList<>();

        // Git 명령어 구성: git clone -b 브랜치명 URL 대상경로
        ProcessBuilder processBuilder = new ProcessBuilder(
                "git", "clone", "-b", branch, url, destDir
        );

        // 표준 에러 스트림(stderr)을 표준 출력(stdout)에 합치기
        // -> 모든 출력(성공/실패 메시지)을 하나의 스트림으로 읽기 위함
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logs.add(line); // log 담기
                }
            }
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                logs.add("Exit code: " + exitCode);
                logs.add(repoName + " Clone Failed");
                return new GitCloneResult(-1, logs);
            } else {
                logs.add("Exit code: " + exitCode);
                logs.add(repoName + " Clone Successful");
                return new GitCloneResult(exitCode, logs);
            }
        } catch (Exception e) {
            logs.add(repoName + " [EXCEPTION] " + e.getMessage());
            return new GitCloneResult(-1, logs);
        } finally {
            for (String logStr : logs) {
                log.info(logStr);
            }
        }
    }
}
