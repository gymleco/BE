package kr.co.gymleco.publicapi;

import kr.co.gymleco.service.inquiry.RateLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * 공개 API 의 오류 응답 형태를 FE 프록시가 아는 {code, message} 로 고정한다.
 *
 * 필드별 상세를 돌려주지 않는 것은 의도다 — 어떤 값이 왜 걸렸는지는
 * 봇에게 학습 자료가 되고, 사람은 FE 의 클라이언트 검증이 먼저 안내한다.
 */
@RestControllerAdvice(basePackages = "kr.co.gymleco.publicapi")
public class PublicApiExceptionHandler {

    @ExceptionHandler(RateLimitExceededException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Map<String, String> rateLimited() {
        return Map.of("code", "RATE_LIMITED",
                      "message", "잠시 후 다시 시도해 주세요.");
    }

    @ExceptionHandler({
        MethodArgumentNotValidException.class,   // @Valid 실패
        HttpMessageNotReadableException.class,   // JSON 깨짐 · 엉뚱한 enum 값
        IllegalArgumentException.class           // 서비스 단 검증 (전화번호 등)
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalid() {
        return Map.of("code", "VALIDATION_FAILED",
                      "message", "필수 항목을 확인해 주세요.");
    }
}
