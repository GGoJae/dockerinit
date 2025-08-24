package com.dockerinit.features.dockerfile.packager.impl;

import com.dockerinit.features.dockerfile.model.ContentType;
import com.dockerinit.features.dockerfile.model.GeneratedFile;
import com.dockerinit.features.dockerfile.model.PackageResult;
import com.dockerinit.features.dockerfile.packager.Packager;
import com.dockerinit.global.exception.InternalErrorCustomException;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ZipPackager implements Packager {

    private static final long IN_MEMORY_THRESHOLD = 5L * 1024 * 1024;   // 5MB
    private static final FileTime EPOCH = FileTime.from(Instant.EPOCH);
    private static final String DEFAULT_PACKAGE_NAME = "docker-artifacts";
    private static final String TEMP_FILE = "docker-artifacts-";

    @Override
    public PackageResult packageFiles(List<GeneratedFile> files, String packageName) {
        Objects.requireNonNull(files, "files");
        if (Objects.isNull(packageName) || packageName.isBlank()) packageName = DEFAULT_PACKAGE_NAME;

        // 현재는 민감 파일이 하나라도 있으면 패키지 전체 민감
        boolean sensitive = files.stream().anyMatch(g -> g.sensitive());

        List<GeneratedFile> ordered = orderAndDedupFilenames(files);

        long packageSize = ordered.stream().mapToLong(f -> f.content().length).sum();

        String zipFileName = packageName.endsWith(".zip") ? packageName : packageName + ".zip";

        try {
            if (packageSize <= IN_MEMORY_THRESHOLD) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream(Math.max(1024, (int) Math.min(packageSize, Integer.MAX_VALUE)));
                MessageDigest md = sha256();
                try (DigestOutputStream dos = new DigestOutputStream(baos, md);
                     ZipOutputStream zos = new ZipOutputStream(dos)) {
                    writeEntriesDeterministic(zos, ordered);
                    zos.finish();
                }
                byte[] zipBytes = baos.toByteArray();
                String etag = strongEtag(md.digest());

                return PackageResult.ofByteArray(zipFileName, ContentType.ZIP, zipBytes, etag, sensitive);
            } else {
                Path tmp = Files.createTempFile(TEMP_FILE, ".zip");
                MessageDigest md = sha256();
                try (OutputStream fos = Files.newOutputStream(tmp, StandardOpenOption.TRUNCATE_EXISTING);
                     DigestOutputStream dos = new DigestOutputStream(fos, md);
                     ZipOutputStream zos = new ZipOutputStream(dos)) {
                    writeEntriesDeterministic(zos, ordered);
                    zos.finish();
                } catch (IOException ioe) {
                    safeDelete(tmp);
                    throw ioe;
                }

                long len = Files.size(tmp);
                String etag = strongEtag(md.digest());

                return PackageResult.ofStreaming(
                        zipFileName,
                        ContentType.ZIP,
                        () -> openDeletingInputStream(tmp),
                        len,
                        etag,
                        sensitive
                );
            }

        } catch (IOException e) {
            throw new InternalErrorCustomException("ZIP 패키지 생성 중 오류");
        }
    }

    private static void writeEntriesDeterministic(ZipOutputStream zos, List<GeneratedFile> files) throws IOException{
        for (GeneratedFile f : files) {
            String name = sanitizeZipPath(f.filename());
            ZipEntry entry = new ZipEntry(name);

            entry.setCreationTime(EPOCH);
            entry.setLastModifiedTime(EPOCH);
            entry.setLastAccessTime(EPOCH);
            zos.putNextEntry(entry);
            zos.write(f.content());
            zos.closeEntry();
        }
    }

    private static List<GeneratedFile> orderAndDedupFilenames(List<GeneratedFile> files) {
        Map<String, Integer> seen = new HashMap<>();
        List<GeneratedFile> normalized = new ArrayList<>(files.size());

        for (GeneratedFile f : files) {
            String raw = (Objects.isNull(f.filename()) || f.filename().isBlank()) ? "file" : f.filename();
            String sane = sanitizeZipPath(raw);
            String unique = makeUniqueName(sane, seen);
            if (!unique.equals(sane)) {
                f = new GeneratedFile(unique, f.content(), f.contentType(), f.sensitive(), f.fileType());
            }
            normalized.add(f);
        }

        normalized.sort(Comparator.comparing(GeneratedFile::filename));
        return normalized;
    }

    private static String sanitizeZipPath(String name) {
        String n = name.replace('\\', '/');

        while (n.startsWith("/")) {
            n = n.substring(1);
        }

        n = n.replace("../", "").replace("..\\", "");
        if (n.isBlank()) n = "file";
        return n;
    }

    private static String makeUniqueName(String base, Map<String, Integer> seen) {
        String candidate = base;
        int idx = seen.getOrDefault(base, 0);
        if (idx == 0) {
            seen.put(base, 1);
            return base;
        }

        String name = base;
        String ext = "";
        int dot = base.lastIndexOf(".");
        if (dot > 0 && dot != base.length() - 1) {
            name = base.substring(0, dot);
            ext = base.substring(dot);
        }
        do {
            idx++;
            candidate = name + " (" + idx + ")" + ext;
        } while (seen.containsKey(candidate));
        seen.put(base, idx);
        seen.put(candidate, 1);
        return candidate;
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e); // TODO 커스텀 예외 만들던가... 아무튼 고려
        }
    }

    private static String strongEtag(byte[] digest) {
        return "\"" + toHex(digest) + "\"";
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >>> 4) & 0xF, 16))
                    .append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    private static void safeDelete(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {}
    }

    private static InputStream openDeletingInputStream(Path path) {
        try {
            return Files.newInputStream(path, StandardOpenOption.DELETE_ON_CLOSE);
        } catch (IOException e) {
            try {
                InputStream in = Files.newInputStream(path);
                return new FilterInputStream(in) {
                    @Override
                    public void close() throws IOException {
                        super.close();
                        safeDelete(path);
                    }
                };
            } catch (IOException ioe) {
                throw new InternalErrorCustomException("ZIP 임시파일 열기 실패", ioe);
            }
        }
    }

}
