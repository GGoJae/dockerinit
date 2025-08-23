package com.dockerinit.features.dockerfile.packager.impl;

import com.dockerinit.features.dockerfile.model.PackageResult;
import com.dockerinit.features.dockerfile.model.GeneratedFile;
import com.dockerinit.features.dockerfile.packager.Packager;
import com.dockerinit.global.exception.InternalErrorCustomException;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ZipPackager implements Packager {

    @Override
    public PackageResult packageFiles(List<GeneratedFile> files, String packageName) {

        // TODO 패키지 담당하는 로직 작성
        return null;
    }

    private byte[] buildDeterministicZip(String dockerfileContent) {
        byte[] zipBytes;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            FileTime epoch = FileTime.from(Instant.EPOCH);
            ZipEntry entry = new ZipEntry("Dockerfile");

            entry.setCreationTime(epoch);
            entry.setLastModifiedTime(epoch);
            entry.setLastAccessTime(epoch);

            zos.putNextEntry(entry);
            zos.write(dockerfileContent.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
            zos.finish();
            zipBytes = baos.toByteArray();
        } catch (IOException e) {
            throw new InternalErrorCustomException("도커 파일 zip byte 민드는 도중 예외", e);
        }
        return zipBytes;
    }
}
