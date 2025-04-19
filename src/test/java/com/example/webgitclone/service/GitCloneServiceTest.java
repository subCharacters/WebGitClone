package com.example.webgitclone.service;

import com.example.webgitclone.dto.GitCloneResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class GitCloneServiceTest {

    @Autowired
    private GitCloneService gitCloneService;
    private final String tempDirPath = System.getProperty("java.io.tmpdir") + "/git-clone-test/";

/*
    각각 따로따로 BeforeEach를 하고 싶다면 이것도 좋을듯.
    @Nested
    @DisplayName("성공 케이스 테스트")
    class SuccessTests {

        @BeforeEach
        void setup() {
            System.out.println("성공 케이스 전용 초기화");
        }

        @Test
        void testCloneSuccess() { ... }
    }
*/

    // 생성된 디렉토리 삭제.
    // 하위를 삭제하지 않으면 에러 나서 하위 모두 삭제함.
    @BeforeEach
    void cleanTempDirectory() throws IOException {
        Path tempDir = Paths.get(tempDirPath);
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder()) // 하위 → 상위
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Test
    @DisplayName("복수개의 URL 정상 클론 확인")
    void cloneMultipleUrls_AllSuccess_Test() throws Exception {
        // given
        List<String> urls = List.of(
                "https://github.com/subCharacters/DesignPattern_Java.git",
                "https://github.com/subCharacters/SpringBatch5_Example.git",
                "https://github.com/subCharacters/SpringBootDB_Connect.git"
        );
        String branch = "master";
        String targetDir = System.getProperty("java.io.tmpdir") + "/git-clone-test/";

        // when
        List<GitCloneResult> results = gitCloneService.cloneMultiple(urls, branch, targetDir);

        // then
        assertEquals(urls.size(), results.size());
        for (GitCloneResult result : results) {
            assertTrue(result.isSuccess(), "클론 실패 로그: " + result.getLogs());
        }
    }
}