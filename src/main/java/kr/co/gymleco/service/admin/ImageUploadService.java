package kr.co.gymleco.service.admin;

import kr.co.gymleco.config.GymlecoProperties;
import kr.co.gymleco.infra.storage.ObjectStorage;
import kr.co.gymleco.support.image.ImageProcessor;
import kr.co.gymleco.support.image.ImageRendition;
import kr.co.gymleco.support.image.InvalidImageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageUploadService {

    private static final Logger log = LoggerFactory.getLogger(ImageUploadService.class);

    private final ImageProcessor processor;
    private final ObjectStorage storage;
    private final GymlecoProperties.Storage config;

    public ImageUploadService(ImageProcessor processor,
                              ObjectStorage storage,
                              GymlecoProperties properties) {
        this.processor = processor;
        this.storage = storage;
        this.config = properties.storage();
    }

    public record UploadResult(String key, Map<String, String> urls) {}

    public UploadResult upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidImageException("파일이 비어 있습니다.");
        }
        if (file.getSize() > config.maxImageBytes()) {
            throw new InvalidImageException(
                "파일이 너무 큽니다. " + (config.maxImageBytes() / 1024 / 1024) + "MB 이하로 올려주세요.");
        }

        byte[] original;
        try {
            original = file.getBytes();
        } catch (IOException e) {
            throw new InvalidImageException("파일을 읽을 수 없습니다.");
        }
        ImageProcessor.Processed processed = processor.process(original);
        String prefix = "p/" + UUID.randomUUID();
        String ext = processed.outputExtension();
        String contentType = ext.equals("png") ? "image/png" : "image/jpeg";

        Map<String, String> urls = new LinkedHashMap<>();
        for (Map.Entry<ImageRendition, byte[]> entry : processed.renditions().entrySet()) {
            String key = prefix + "/" + entry.getKey().fileName(ext);
            storage.put(key, entry.getValue(), contentType);
            urls.put(String.valueOf(entry.getKey().maxWidth()), storage.publicUrl(key));
        }

        log.info("이미지 업로드 완료: {} ({} → {})",
            prefix, processed.sourceFormat(), ext);
        return new UploadResult(prefix + "." + ext, urls);
    }

    public void delete(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        if (!key.matches("^p/[0-9a-fA-F-]{36}\\.(jpg|png)$")) {
            throw new IllegalArgumentException("올바르지 않은 이미지 키입니다.");
        }
        storage.delete(key.substring(0, key.lastIndexOf('.')));
    }
}
