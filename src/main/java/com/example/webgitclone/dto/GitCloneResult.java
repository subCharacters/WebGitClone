package com.example.webgitclone.dto;

import java.util.List;

public class GitCloneResult {
    private final int exitCode;
    private final List<String> logs;

    public GitCloneResult(int exitCode, List<String> logs) {
        this.exitCode = exitCode;
        this.logs = logs;
    }

    public boolean isSuccess() {
        return exitCode == 0;
    }

    public int getExitCode() {
        return exitCode;
    }

    public List<String> getLogs() {
        return logs;
    }
}
