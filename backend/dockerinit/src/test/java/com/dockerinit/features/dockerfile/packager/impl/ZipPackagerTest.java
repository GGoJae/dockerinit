package com.dockerinit.features.dockerfile.packager.impl;

import com.dockerinit.features.model.ContentType;
import com.dockerinit.features.dockerfile.domain.DockerFileType;
import com.dockerinit.features.model.GeneratedFile;
import com.dockerinit.features.model.PackageResult;
import com.dockerinit.features.packager.impl.ZipPackager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ZipPackagerTest {

    private final ZipPackager packager = new ZipPackager();

    @Test
    @DisplayName("패키지 크기가 5MB 이하이고 동일한 파일이 들어있을때 etag 의 값이 같은가?")
    void is_same_etag_when_same_file_in_package() throws Exception{
        GeneratedFile a = new GeneratedFile("a.txt", "hello".getBytes(), ContentType.TEXT, false, DockerFileType.README);
        GeneratedFile b = new GeneratedFile("b.txt", "world".getBytes(), ContentType.TEXT, false, DockerFileType.README);

        PackageResult p1 = packager.packageFiles(List.of(a, b), "pkg");
        PackageResult p2 = packager.packageFiles(List.of(b, a), "pkg"); // 순서 바꿔도 동일해야 함

        byte[] zip1 = p1.fold(bs -> bs, s -> readAll(s.get()));
        byte[] zip2 = p2.fold(bs -> bs, s -> readAll(s.get()));
        assertThat(zip1).isEqualTo(zip2);
        assertThat(p1.getEtag()).isEqualTo(p2.getEtag());
        assertThat(p1.getFilename()).isEqualTo("pkg.zip");
        assertThat(p1.getContentType().value()).isEqualTo("application/zip");

        assertThat(listEntries(zip1)).containsExactly("a.txt", "b.txt");
    }

    @Test
    @DisplayName("패키지 크기가 5MB 이하이고 다른 파일이 들어있을때 etag 의 값이 다른가?")
    void is_not_same_etag_when_different_file_in_package() throws Exception{
        GeneratedFile a = new GeneratedFile("a.txt", "hello".getBytes(), ContentType.TEXT, false, DockerFileType.README);
        GeneratedFile b = new GeneratedFile("b.txt", "world".getBytes(), ContentType.TEXT, false, DockerFileType.README);
        GeneratedFile c = new GeneratedFile("c.txt", "java".getBytes(), ContentType.TEXT, false, DockerFileType.README);

        PackageResult p1 = packager.packageFiles(List.of(a, b), "pkg");
        PackageResult p2 = packager.packageFiles(List.of(a, c), "pkg"); // 순서 바꿔도 동일해야 함

        byte[] zip1 = p1.fold(bs -> bs, s -> readAll(s.get()));
        byte[] zip2 = p2.fold(bs -> bs, s -> readAll(s.get()));
        assertThat(zip1).isNotEqualTo(zip2);
        assertThat(p1.getEtag()).isNotEqualTo(p2.getEtag());
        assertThat(p1.getFilename()).isEqualTo("pkg.zip");
        assertThat(p1.getContentType().value()).isEqualTo("application/zip");

        assertThat(listEntries(zip1)).containsExactly("a.txt", "b.txt");
        assertThat(listEntries(zip2)).containsExactly("a.txt", "c.txt");
    }

    @Test
    @DisplayName("내용이 다르면 ETag가 달라지는가?")
    void etag_changes_when_content_difference() {
        GeneratedFile a1 = new GeneratedFile("a.txt", "hello".getBytes(), ContentType.TEXT, false, DockerFileType.README);
        GeneratedFile a2 = new GeneratedFile("a.txt", "hello!".getBytes(), ContentType.TEXT, false, DockerFileType.README);

        PackageResult p1 = packager.packageFiles(List.of(a1), "pkg");
        PackageResult p2 = packager.packageFiles(List.of(a2), "pkg");

        assertThat(p1.getEtag()).isNotEqualTo(p2.getEtag());
    }

    @Test
    @DisplayName("packageName이 비었을 때 기본 파일명(docker-artifacts.zip)으로 생성되는가?")
    void default_package_name_when_blank() {
        GeneratedFile a = new GeneratedFile("a.txt", "x".getBytes(), ContentType.TEXT, false, DockerFileType.README);

        PackageResult p = packager.packageFiles(List.of(a), "");
        assertThat(p.getFilename()).isEqualTo("docker-artifacts.zip");
    }

    @Test
    @DisplayName("민감 파일이 하나라도 있으면 패키지 전체가 민감으로 표시되는가?")
    void sensitive_flag_bubbles_up() {
        GeneratedFile safe = new GeneratedFile("a.txt", "x".getBytes(), ContentType.TEXT, false, DockerFileType.README);
        GeneratedFile secret = new GeneratedFile("secret.env", "TOKEN=xxx".getBytes(), ContentType.TEXT, true, DockerFileType.ENV);

        PackageResult p = packager.packageFiles(List.of(safe, secret), "pkg");
        assertThat(p.isSensitive()).isTrue();
    }

    @Test
    @DisplayName("중복 파일명은 n 표시로 유니크 처리되는가?")
    void duplicate_file_names_get_unq_filename() throws Exception{
        GeneratedFile f1 = new GeneratedFile("text.txt", "a".getBytes(), ContentType.TEXT, false, DockerFileType.README);
        GeneratedFile f2 = new GeneratedFile("text.txt", "b".getBytes(), ContentType.TEXT, false, DockerFileType.README);
        GeneratedFile f3 = new GeneratedFile("text.txt", "c".getBytes(), ContentType.TEXT, false, DockerFileType.README);

        PackageResult p = packager.packageFiles(List.of(f1, f2, f3), "pkg");
        byte[] zip = p.fold(bs -> bs, s -> readAll(s.get()));

        assertThat(listEntries(zip)).contains("text.txt", "text (2).txt", "text (3).txt");
    }

    @Test
    @DisplayName("확장자가 여러 개인 경우에도 (n)는 마지막 확장자 앞에 붙는다 (e.g. .tar.gz)")
    void numbered_suffix_before_last_extension() throws Exception {
        GeneratedFile f1 = new GeneratedFile("archive.tar.gz", "1".getBytes(), ContentType.OCTET, false, DockerFileType.README);
        GeneratedFile f2 = new GeneratedFile("archive.tar.gz", "2".getBytes(), ContentType.OCTET, false, DockerFileType.README);

        PackageResult p = packager.packageFiles(List.of(f1, f2), "pkg");
        byte[] zip = p.fold(bs -> bs, s -> readAll(s.get()));

        assertThat(listEntries(zip)).containsExactly("archive.tar (2).gz", "archive.tar.gz");
    }

    @Test
    @DisplayName("경로 정규화: 루트/상위경로/백슬래시 제거 및 슬래시 통일")
    void path_sanitization_rules() throws Exception {
        GeneratedFile f1 = new GeneratedFile("/root/../etc/hosts", "x".getBytes(), ContentType.TEXT, false, DockerFileType.README);
        GeneratedFile f2 = new GeneratedFile("a\\b\\c.txt", "y".getBytes(), ContentType.TEXT, false, DockerFileType.README);

        PackageResult p = packager.packageFiles(List.of(f1, f2), "pkg");
        byte[] zip = p.fold(bs -> bs, s -> readAll(s.get()));

        assertThat(listEntries(zip)).containsExactly("a/b/c.txt", "etc/hosts");
    }

    @Test
    @DisplayName("인메모리 ZIP은 contentLength()가 ZIP 바이트 길이와 일치한다")
    void content_length_matches_in_memory_zip() {
        GeneratedFile a = new GeneratedFile("a.txt", "hello".getBytes(), ContentType.TEXT, false, DockerFileType.README);

        PackageResult p = packager.packageFiles(List.of(a), "pkg");
        byte[] zip = p.fold(bs -> bs, s -> readAll(s.get()));

        assertThat(p.contentLength().isPresent()).isTrue();
        assertThat(p.contentLength().getAsLong()).isEqualTo(zip.length);
    }

    @Test
    @DisplayName("ETag는 따옴표로 감싼 64자리 hex(sha-256) 형식이다")
    void etag_format_is_strong_sha256() {
        GeneratedFile a = new GeneratedFile("a.txt", "x".getBytes(), ContentType.TEXT, false, DockerFileType.README);

        PackageResult p = packager.packageFiles(List.of(a), "pkg");
        assertThat(p.getEtag()).matches("^\"[0-9a-f]{64}\"$");
    }

    @Test
    @DisplayName("6MB의 패키지는 스트리밍 패키징이 되는가?")
    void streamingPackaging_largeFile_switchesToStreamingAndSetsLength() {
        byte[] big = new byte[6 * 1024 * 1024];
        GeneratedFile bigFile = new GeneratedFile("big.bin", big, ContentType.OCTET, false, DockerFileType.README);

        PackageResult pkg = packager.packageFiles(List.of(bigFile), "huge");
        assertThat(pkg.contentLength().isPresent()).isTrue();
        // fold 호출이 스트리밍 경로로 가는지 확인
        Boolean isStreaming = pkg.fold(bs -> false, s -> true);
        assertThat(isStreaming).isTrue();
    }

    @Test
    @DisplayName("5MB의 패키지는 인메모리 패키징이 되는가?")
    void inMemoryPackaging_largeFile_switchesToStreamingAndSetsLength() {
        byte[] big = new byte[5 * 1024 * 1024];
        GeneratedFile bigFile = new GeneratedFile("big.bin", big, ContentType.OCTET, false, DockerFileType.README);

        PackageResult pkg = packager.packageFiles(List.of(bigFile), "huge");
        assertThat(pkg.contentLength().isPresent()).isTrue();
        // fold 호출이 스트리밍 경로로 가는지 확인
        Boolean isStreaming = pkg.fold(bs -> false, s -> true);
        assertThat(isStreaming).isFalse();
    }

    @Test
    @DisplayName("파일이름들 정규화되어 정렬되는가??")
    void sanitize_and_dedup_names() throws Exception {
        GeneratedFile x = new GeneratedFile("../../etc/passwd", "x".getBytes(), ContentType.TEXT, false, DockerFileType.README);
        GeneratedFile y = new GeneratedFile("/abs/path\\to\\file.txt", "y".getBytes(), ContentType.TEXT, false, DockerFileType.README);
        GeneratedFile dup = new GeneratedFile("file.txt", "z".getBytes(), ContentType.TEXT, false, DockerFileType.README);

        PackageResult pkg = packager.packageFiles(List.of(x, y, dup), "s");
        byte[] zip = pkg.fold(bs -> bs, s -> {
            try {
                return s.get().readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        assertThat(listEntries(zip)).containsExactly(
                "abs/path/to/file.txt",
                "etc/passwd",
                "file.txt"
        );
    }

    @DisplayName("Windows 드라이브 프리픽스 제거")
    @Test void windows_drive_prefix_removed() throws Exception {
        GeneratedFile f = new GeneratedFile("C:\\temp\\x.txt", "x".getBytes(), ContentType.TEXT, false, DockerFileType.README);
        var p = packager.packageFiles(List.of(f), "pkg");
        var zip = p.fold(bs -> bs, s -> {
            try {
                return s.get().readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        assertThat(listEntries(zip)).containsExactly("temp/x.txt");
    }

    @DisplayName("여러 '..'와 '.' 세그먼트가 안전하게 정규화된다")
    @Test void dotdot_collapses_safely() throws Exception {
        GeneratedFile f = new GeneratedFile("a/./b/../../c/../d/e.txt", "x".getBytes(), ContentType.TEXT, false, DockerFileType.README);
        var p = packager.packageFiles(List.of(f), "pkg");
        var zip = p.fold(bs -> bs, s -> {
            try {
                return s.get().readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        assertThat(listEntries(zip)).containsExactly("d/e.txt");
    }


    /* helpers */
    private static byte[] readAll(InputStream in) {
        try (in) {
            return in.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String> listEntries(byte[] zipBytes) throws Exception {
        try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            new java.util.ArrayList<>();
            var names = new java.util.ArrayList<String>();
            ZipEntry e;
            while ((e = zin.getNextEntry()) != null) {
                names.add(e.getName());
            }
            return names;
        }
    }
}