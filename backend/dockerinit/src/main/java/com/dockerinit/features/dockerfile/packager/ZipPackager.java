package com.dockerinit.features.dockerfile.packager;

import com.dockerinit.features.dockerfile.model.GeneratedFile;

import java.util.List;

public class ZipPackager implements ArtifactPackager {

    @Override
    public byte[] toZip(String dockerfile, List<GeneratedFile> extras) {
        return null; // TODO zip 파일 바이트 리턴 로직 작성
    }
}
