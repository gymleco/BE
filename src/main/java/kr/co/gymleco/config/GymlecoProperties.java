package kr.co.gymleco.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;
@Validated
@ConfigurationProperties(prefix = "gymleco")
public record GymlecoProperties(
    @NotNull @Valid Auth auth,
    @NotNull @Valid Pii pii,
    @NotNull Admin admin,
    @NotNull @Valid Inquiry inquiry,
    @NotNull @Valid Cors cors,
    @Valid Revalidate revalidate,
    @Valid Storage storage
) {

    public record Auth(
        @NotBlank @Size(min = 32) String jwtSecret,
        @Positive int accessTtlMinutes,
        @Positive int refreshTtlDays,
        String cookieDomain,
        boolean cookieSecure,
        @Positive int maxAttemptsPerIp,
        @Positive int ipLockMinutes
    ) {
        public Duration accessTtl() {
            return Duration.ofMinutes(accessTtlMinutes);
        }

        public Duration refreshTtl() {
            return Duration.ofDays(refreshTtlDays);
        }

        public Duration ipLockDuration() {
            return Duration.ofMinutes(ipLockMinutes);
        }
    }

    public record Pii(
        /** AES-256 키 (base64, 디코딩 시 32바이트) */
        @NotBlank String encryptionKey,
        /** 블라인드 인덱스용 HMAC 키. 위 키와 반드시 달라야 한다. */
        @NotBlank String blindIndexKey
    ) {}

    public record Inquiry(
        @Positive int retentionDays,
        @Positive int rateLimitPerHour,
        String notifyTo
    ) {
        public Duration retention() {
            return Duration.ofDays(retentionDays);
        }
    }

    public record Cors(
        @NotNull List<String> allowedOrigins
    ) {}
    public record Revalidate(String url, String token) {
        public boolean isConfigured() {
            return url != null && !url.isBlank()
                && token != null && !token.isBlank();
        }
    }
    public record Storage(
        String bucket,
        String region,
        String cdnBaseUrl,
        List<String> allowedImageTypes,
        long maxImageBytes
    ) {}
    public record Admin(String bootstrapUsername, String bootstrapPassword){
        public boolean isConfigured(){
            return bootstrapUsername != null && !bootstrapUsername.isBlank() && bootstrapPassword != null && !bootstrapPassword.isBlank();
        }
    }
}
