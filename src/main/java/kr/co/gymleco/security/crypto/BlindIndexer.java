package kr.co.gymleco.security.crypto;

import kr.co.gymleco.config.GymlecoProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
@Component
public class BlindIndexer {

    private static final String ALGORITHM = "HmacSHA256";

    private final byte[] keyBytes;

    public BlindIndexer(GymlecoProperties properties) {
        this.keyBytes = Base64.getDecoder()
            .decode(properties.pii().blindIndexKey());
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                "PII_BLIND_INDEX_KEY 는 32바이트 이상이어야 합니다.");
        }
        if (java.util.Arrays.equals(keyBytes,
            Base64.getDecoder().decode(properties.pii().encryptionKey()))) {
            throw new IllegalStateException(
                "블라인드 인덱스 키가 암호화 키와 같습니다. 서로 다른 값을 쓰세요.");
        }
    }

    /** DB 의 phone_blind_index (BYTEA) 에 그대로 저장한다. */
    public byte[] index(String normalizedPhone) {
        if (normalizedPhone == null || normalizedPhone.isBlank()) {
            return null;
        }
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(keyBytes, ALGORITHM));
            return mac.doFinal(normalizedPhone.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("블라인드 인덱스 생성에 실패했습니다.", e);
        }
    }
}
