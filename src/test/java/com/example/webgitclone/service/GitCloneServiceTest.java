package com.example.webgitclone.service;

import com.example.webgitclone.dto.GitCloneResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
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
    void cloneMultipleUrls_AllSuccess_Test(CapturedOutput output) throws Exception {
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
        assertTrue(output.getOut().contains("Clone Successful"));
    }

    @Test
    @DisplayName("복수개의 URL 중 일부 실패시에도 나머지 정상 동작 확인")
    void cloneMultipleUrls_PartialFailure_Test() throws Exception {
        // given
        List<String> urls = List.of(
                "https://github.com/subCharacters/this-repo-does-not-exist.git",
                "https://github.com/subCharacters/SpringBatch5_Example.git",
                "https://github.com/subCharacters/SpringBootDB_Connect.git"
        );
        String branch = "master";
        String targetDir = System.getProperty("java.io.tmpdir") + "/git-clone-test/";
        // when
        List<GitCloneResult> results = gitCloneService.cloneMultiple(urls, branch, targetDir);

        // then
        assertEquals(urls.size(), results.size());

        GitCloneResult first = results.get(0);
        assertFalse(first.isSuccess(), "첫 번째 URL 실패");
        assertTrue(first.getLogs().stream().anyMatch(log -> log.contains("Clone Failed")), "실패 로그 유무 확인");

        GitCloneResult second = results.get(1);
        assertTrue(second.getLogs().stream().anyMatch(log -> log.contains("SpringBatch5_Example Clone Successful")), "성공 로그 유무 확인");
        assertTrue(second.isSuccess(), "두 번째 URL 성공");

        GitCloneResult third = results.get(2);
        assertTrue(third.getLogs().stream().anyMatch(log -> log.contains("SpringBootDB_Connect Clone Successful")), "성공 로그 유무 확인");
        assertTrue(third.isSuccess(), "세 번째 URL 성공");
    }

    @Test
    @DisplayName("예외 발생 시 다음 URL로 넘어가서 클론 진행 확인")
    void cloneMultipleUrls_ExceptionDuringClone_Test() throws Exception {
        // given
        List<String> urls = List.of(
                "https://github.com/subCharacters/this-repo-does-not-exist.git",
                "https://github.com/subCharacters/SpringBatch5_Example.git",
                "https://github.com/subCharacters/SpringBootDB_Connect.git"
        );
        String branch = "master";
        String targetDir = System.getProperty("java.io.tmpdir") + "/git-clone-test/";

        AtomicBoolean isFirst = new AtomicBoolean(true);
        // 첫번째만 예외 발생 시키고 나머지는 실제 clone을 수행.
        GitCloneExecutor testExecutor = (url, br, dest, repo) -> {
            if (isFirst.getAndSet(false)) {
                List<String> logs = List.of(repo + " [EXCEPTION] fist url exception");
                return new GitCloneResult(-1, logs);
            }
            return new GitCloneService().cloneMultiple(List.of(url), br, targetDir).get(0);
        };

        GitCloneService service = new GitCloneService(testExecutor);
        // when
        List<GitCloneResult> results = service.cloneMultiple(urls, branch, targetDir);

        // then
        assertEquals(urls.size(), results.size());

        GitCloneResult first = results.get(0);
        assertFalse(first.isSuccess(), "첫 번째 URL은 예외 발생으로 실패해야 함");
        assertTrue(first.getLogs().stream().anyMatch(log -> log.contains("[EXCEPTION]")), "예외 메시지 포함 여부");

        GitCloneResult second = results.get(1);
        assertTrue(second.isSuccess(), "두 번째 URL은 성공해야 함");
        assertTrue(second.getLogs().stream().anyMatch(log -> log.contains("SpringBatch5_Example Clone Successful")), "성공 로그 유무 확인");

        GitCloneResult third = results.get(2);
        assertTrue(third.isSuccess(), "세 번째 URL은 성공해야 함");
        assertTrue(third.getLogs().stream().anyMatch(log -> log.contains("SpringBootDB_Connect Clone Successful")), "성공 로그 유무 확인");

    }
}