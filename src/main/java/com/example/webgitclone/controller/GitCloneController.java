package com.example.webgitclone.controller;

import com.example.webgitclone.service.GitCloneService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Controller
public class GitCloneController {

    @GetMapping("/git-clone")
    public String gitClonePage() {
        return "clone";
    }

    @PostMapping("/clone")
    public String gitClonePage(
            @RequestParam(value = "branch", required = false) String branch,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "targetDir", required = false)String targetDir,
            Model model) {
        if (StringUtils.isEmpty(branch)) {
            model.addAttribute("error", "브랜치 명을 입력해주세요.");
            return "clone";
        }

        if (file == null) {
            model.addAttribute("error", "파일을 업로드해주세요.");
            return "clone";
        }

        if (file.isEmpty()) {
            model.addAttribute("error", "파일이 비어있습니다.");
            return "clone";
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".txt")) {
            model.addAttribute("error", "txt 파일만 업로드 가능합니다.");
            return "clone";
        }

        if (StringUtils.isEmpty(targetDir)) {
            model.addAttribute("error", "클론할 경로를 입력하세요.");
            return "clone";
        }

        if (targetDir.startsWith("~")) {
            model.addAttribute("error", "~로 시작하는 경로는 입력할 수 없습니다.");
            return "clone";
        }

        File dir = new File(targetDir);
        if (!dir.exists() || !dir.isDirectory()) {
            model.addAttribute("error", "입력한 경로가 존재하지 않습니다.");
            return "clone";
        }

        return "clone";
    }
}
