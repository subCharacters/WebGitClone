package com.example.webgitclone.service;

import com.example.webgitclone.dto.GitCloneResult;

@FunctionalInterface
public interface GitCloneExecutor {
    GitCloneResult execute(String url, String branch, String destDir, String repoName);
}
