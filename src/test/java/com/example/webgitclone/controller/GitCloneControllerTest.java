package com.example.webgitclone.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GitCloneController.class) // 컨트롤러 단위 테스트를 위한 어노테이션
class GitCloneControllerTest {

    @Autowired
    private MockMvc mockMvc; // 가짜 HTTP 요청을 만들어주는 테스트 도구

    @Test
    @DisplayName("브랜치명이 비어있으면 에러 메시지를 반환")
    void gitClone_BranchNameMissing_Test() throws Exception {
        // 가짜 업로드 파일 생성 (정상적인 txt 파일)
        MockMultipartFile file = new MockMultipartFile(
                "file" // 파라미터 이름
                , "repos.txt" // 파일 이름
                , "text/plain" // MIME 타입
                , "https://github.com/example".getBytes() // 파일 내용
        );
        mockMvc.perform(multipart("/clone") // 파일 업로드가 포함된 HTTP 요청 방식
                .file(file)
                .param("branch", "")
                .param("targetDir", "C:\\Users\\example"))
                .andExpect(status().isOk())
                .andExpect(view().name("clone"))
                .andExpect(model().attribute("error", "브랜치 명을 입력해주세요."))
                .andDo(print());
    }

    @Test
    @DisplayName("txt파일이 없는 경우 에러 메시지를 반환")
    void gitClone_FileParameterMissing_Test() throws Exception{
        mockMvc.perform(multipart("/clone")
                        .param("targetDir", "C:\\Users\\example")
                        .param("branch", "master"))
                .andExpect(status().isOk())
                .andExpect(view().name("clone"))
                .andExpect(model().attribute("error", "파일을 업로드해주세요."))
                .andDo(print());
    }

    @Test
    @DisplayName("txt파일이 비어있는 경우 에러 메시지를 반환")
    void gitClone_FileEmpty_Test() throws Exception{
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.txt", "text/plain", new byte[0] // 0바이트
        );
        mockMvc.perform(multipart("/clone")
                        .file(emptyFile)
                        .param("targetDir", "C:\\Users\\example")
                        .param("branch", "master"))
                .andExpect(status().isOk())
                .andExpect(view().name("clone"))
                .andExpect(model().attribute("error", "파일이 비어있습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("txt가 아닐경우 에러 메시지를 반환")
    void gitClone_FileExtensionMissing_Test() throws Exception{
        MockMultipartFile file = new MockMultipartFile(
                "file"
                , "repos.csv"
                , "text/csv"
                , "https://github.com/example".getBytes()
        );
        mockMvc.perform(multipart("/clone")
                .file(file)
                .param("targetDir", "C:\\Users\\example")
                .param("branch", "master"))
                .andExpect(status().isOk())
                .andExpect(view().name("clone"))
                .andExpect(model().attribute("error", "txt 파일만 업로드 가능합니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("targetDir이 비어 있는 경우 에러 메시지를 반환")
    void gitClone_TargetDirMissing_Test() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file"
                , "repos.txt"
                , "text/plain"
                , "https://github.com/example".getBytes()
        );
        mockMvc.perform(multipart("/clone")
                .file(file)
                .param("targetDir", "")
                .param("branch", "master"))
                .andExpect(status().isOk())
                .andExpect(view().name("clone"))
                .andExpect(model().attribute("error", "클론할 경로를 입력하세요."))
                .andDo(print());
    }

    @Test
    @DisplayName("targetDir가 ~로 시작하면 에러 메시지를 반환")
    void gitClone_TargetDirStartWithTilde_Test() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file"
                , "repos.txt"
                , "text/plain"
                , "https://github.com/example".getBytes()
        );
        mockMvc.perform(multipart("/clone")
                        .file(file)
                        .param("targetDir", "~/projects")
                        .param("branch", "master"))
                .andExpect(status().isOk())
                .andExpect(view().name("clone"))
                .andExpect(model().attribute("error", "~로 시작하는 경로는 입력할 수 없습니다."))
                .andDo(print());
    }

}