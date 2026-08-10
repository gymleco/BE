package kr.co.gymleco.security.crypto;

import kr.co.gymleco.config.GymlecoProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
@Component
public class PiiEncryptor {
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;
    private static final int KEY_BYTES = 32;
    private final SecretKey key;
    private final SecureRandom random = new SecureRandom();
    public PiiEncryptor(GymlecoProperties properties) {
        byte[] raw;
        try {
            raw = Base64.getDecoder().decode(properties.pii().encryptionKey());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                "PII_ENCRYPTION_KEY 가 base64 가 아닙니다. openssl rand -base64 32", e);
        }
        if (raw.length != KEY_BYTES) {
            throw new IllegalStateException(
                "PII_ENCRYPTION_KEY 는 32바이트여야 합니다. 현재 " + raw.length + "바이트");
        }
        this.key = new SecretKeySpec(raw, "AES");
        Arrays.fill(raw, (byte) 0);
    }
    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_BYTES];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] sealed = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + sealed.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(sealed, 0, out, iv.length, sealed.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("개인정보 암호화에 실패했습니다.", e);
        }
    }
    public String decrypt(String encoded) {
        if (encoded == null) {
            return null;
        }
        try {
            byte[] all = Base64.getDecoder().decode(encoded);
            if (all.length <= IV_BYTES) {
                throw new IllegalStateException("암호문 길이가 올바르지 않습니다.");
            }
            byte[] iv = Arrays.copyOfRange(all, 0, IV_BYTES);
            byte[] sealed = Arrays.copyOfRange(all, IV_BYTES, all.length);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(sealed), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("개인정보 복호화에 실패했습니다.", e);
        }
    }
}
