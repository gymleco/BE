package kr.co.gymleco.security.login;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import kr.co.gymleco.config.GymlecoProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 로그인 무차별 대입 방어
 */
@Component
public class LoginAttemptGuard {
    private final Cache<String, AtomicInteger> ipFailures;
    private final Cache<String, AtomicInteger> accountFailures;
    private final int maxPerIp;
    public LoginAttemptGuard(GymlecoProperties properties){
        GymlecoProperties.Auth auth = properties.auth();
        this.maxPerIp  = auth.maxAttemptsPerIp();
        Duration window = auth.ipLockDuration();
        // expireAfterWrite: 마지막 실패로부터 window 가 지나면 초기화.
        // maximumSize 는 메모리 폭주 방지용 상한
        this.ipFailures = Caffeine.newBuilder()
            .expireAfterWrite(window)
            .maximumSize(10_000)
            .build();
        this.accountFailures = Caffeine.newBuilder()
            .expireAfterWrite(window)
            .maximumSize(1_000)
            .build();
    }
    public boolean isBlocked(String ip){
        AtomicInteger count = ipFailures.getIfPresent(key(ip));
        return count != null && count.get() >= maxPerIp;
    }
    public Duration penaltyFor(String username) {
        AtomicInteger count = accountFailures.getIfPresent(key(username));
        if (count == null || count.get() <= 2) {
            return Duration.ZERO;
        }
        long millis = Math.min(4000L, (count.get() - 2) * 500L);
        return Duration.ofMillis(millis);
    }
    public void recordFailure(String ip, String username) {
        ipFailures.get(key(ip), k -> new AtomicInteger()).incrementAndGet();
        accountFailures.get(key(username), k -> new AtomicInteger()).incrementAndGet();
    }
    public void recordSuccess(String ip, String username){
        ipFailures.invalidate(key(ip));
        accountFailures.invalidate(key(username));
    }
    private String key(String raw){
        return raw == null ? "-" : raw.trim().toLowerCase();
    }
}
