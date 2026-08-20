package kr.co.gymleco.infra.storage;

public interface ObjectStorage {
    String put(String key, byte[] content, String contentType);
    void delete(String keyPrefix);
    String publicUrl(String key);
}
