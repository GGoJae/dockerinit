package com.dockerinit.features.preset.materializer;

import com.dockerinit.features.model.FileType;
import com.dockerinit.features.model.GeneratedFile;
import com.dockerinit.features.preset.domain.ContentStrategy;
import com.dockerinit.features.preset.domain.PresetArtifact;
import com.dockerinit.features.preset.mapper.PresetMapper;
import com.dockerinit.global.exception.InvalidInputCustomException;
import com.dockerinit.global.exception.UnsupportedOperationCustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PresetArtifactMaterializer {

    // TODO 나중에 Object Storage 도입하면 여기에 주입받고 처리

    private GeneratedFile toGeneratedFile(PresetArtifact artifact) {
        // TODO storage 에 담았을때 스토리지 키로 컨텐츠 가져오는 로직
        if (artifact.getStrategy() == ContentStrategy.OBJECT_STORAGE) {
            throw new UnsupportedOperationCustomException("OBJECT_STORAGE 는 아직 지원하지 않습니다.", Map.of("strategy", artifact.getStrategy()));
        }
        String content = Objects.requireNonNull(artifact.getEmbeddedContent(), "EMBEDED preset artifact 가 content 를 가지고 있지않습니다");
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
        return new GeneratedFile(artifact.getFilename(), contentBytes, artifact.getContentType(), artifact.getSensitive(), artifact.getFileType());
    }

    public List<GeneratedFile> toGeneratedFiles(List<PresetArtifact> artifacts, Set<FileType> targets) {
        return artifacts.stream()
                .filter(a -> targets.contains(a.getFileType()))
                .map(this::toGeneratedFile)
                .toList();
    }

}
