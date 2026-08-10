package kr.co.gymleco.security.crypto;

import kr.co.gymleco.config.GymlecoProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

/**
 * 검색용 블라인드 인덱스.
 *
 * HMAC-SHA256(정규화된 번호) 를 별도 컬럼에 저장한다.
 * 복호화는 불가능하지만 "같은 번호인가" 는 판별된다.
 *
 * ★ 키는 PiiEncryptor 의 암호화 키와 반드시 달라야 한다.
 *   같은 키를 쓰면 인덱스 값에서 암호화 키에 대한 정보가 샌다.
 *
 * ★ 단순 SHA-256 을 쓰면 안 된다. 국내 휴대폰 번호는 경우의 수가
 *   1억 개 남짓이라 무지개 표로 전수 역산이 가능하다.
 *   키가 들어간 HMAC 이어야 키 없이는 역산할 수 없다.
 */
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
