package kr.co.gymleco.admin;

import kr.co.gymleco.service.admin.AuthenticationFailedException;
import kr.co.gymleco.service.admin.DuplicateSlugException;
import kr.co.gymleco.service.admin.BannerNotFoundException;
import kr.co.gymleco.service.admin.InquiryNotFoundException;
import kr.co.gymleco.service.admin.ProductNotFoundException;
import kr.co.gymleco.service.admin.SupportNotFoundException;
import kr.co.gymleco.service.admin.UsedItemNotFoundException;
import kr.co.gymleco.support.image.InvalidImageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.MismatchedInputException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice(basePackages = "kr.co.gymleco.admin")
public class AdminApiExceptionHandler {
    /** "값이 없다" 계열 제약. 같은 칸에서 형식 제약보다 먼저 알린다. */
    private static final Set<String> REQUIRED = Set.of("NotNull", "NotBlank", "NotEmpty");

    @ExceptionHandler(AuthenticationFailedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> authFailed(){return Map.of("code", "INVALID_CREDENTIALS", "message", "아이디 또는 비밀번호가 올바르지 않습니다.");}
    /**
     * 어느 칸이 틀렸는지 함께 내려준다.
     *
     * "입력값을 확인해 주세요" 만 오면 관리자가 어느 칸을 고쳐야 할지
     * 알 수 없다. 배너처럼 필수 칸이 여러 개인 화면에서는 특히 그렇다.
     *
     * ★ 관리 API 에서만 이렇게 한다.
     *   공개 API(PublicApiExceptionHandler)는 지금처럼 뭉뚱그린다 —
     *   필드명과 제약을 밖으로 흘리면 스키마가 드러난다.
     *   관리 API 는 이미 로그인한 사람만 닿는다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> invalid(MethodArgumentNotValidException e){
        Map<String, String> fields = new LinkedHashMap<>();
        // 한 칸에 제약이 여러 개 걸릴 수 있다. 빈 slug 는 NotBlank 와 Pattern 이
        // 동시에 터지는데, 이때 "형식이 올바르지 않습니다" 보다 "입력해 주세요" 가
        // 먼저 나와야 한다. 값이 없는 것과 값이 틀린 것은 고치는 방법이 다르다.
        // 제약이 발견되는 순서는 보장되지 않으므로 여기서 직접 세운다.
        e.getBindingResult().getFieldErrors().stream()
            .sorted(Comparator.comparingInt(fe -> REQUIRED.contains(fe.getCode()) ? 0 : 1))
            .forEach(fe -> fields.putIfAbsent(fe.getField(),
                fe.getDefaultMessage() == null ? "값을 확인해 주세요." : fe.getDefaultMessage()));
        return Map.of(
            "code", "VALIDATION_FAILED",
            // 같은 문구가 여러 칸에서 나오면(대개 "필수 입력 항목입니다.") 한 번만 잇는다.
            // 어느 칸인지는 아래 fields 가 알려주므로 문구를 반복할 이유가 없다.
            "message", fields.isEmpty()
                ? "입력값을 확인해 주세요."
                : String.join(" ", new LinkedHashSet<>(fields.values())),
            "fields", fields);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> unreadable(HttpMessageNotReadableException e){
        if (e.getCause() instanceof MismatchedInputException mie) {
            String field = jsonPath(mie);
            String message = mie instanceof InvalidFormatException ife
                ? describe(ife) : "값의 형식이 올바르지 않습니다.";
            if (field != null) {
                return Map.of("code", "VALIDATION_FAILED", "message", message,
                              "fields", Map.of(field, message));
            }
        }
        return Map.of("code", "MALFORMED_JSON", "message", "요청 형식이 올바르지 않습니다.");
    }

    private static String jsonPath(MismatchedInputException e){
        StringBuilder sb = new StringBuilder();
        for (JacksonException.Reference ref : e.getPath()) {
            if (ref.getPropertyName() == null) continue;
            if (!sb.isEmpty()) sb.append('.');
            sb.append(ref.getPropertyName());
        }
        return sb.isEmpty() ? null : sb.toString();
    }

    private static String describe(InvalidFormatException e){
        Class<?> target = e.getTargetType();
        if (target != null && target.isEnum()) {
            String allowed = Arrays.stream(target.getEnumConstants())
                .map(String::valueOf).collect(Collectors.joining(", "));
            return "고를 수 있는 값: " + allowed;
        }
        return "값의 형식이 올바르지 않습니다.";
    }

    @ExceptionHandler(DuplicateSlugException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> duplicateSlug(DuplicateSlugException e){return Map.of("code", "DUPLICATE_SLUG", "message", e.getMessage());}
    /*
     * 관리 API 의 "대상 없음" 을 한자리에 모은다.
     * 여기에 빠진 예외는 전역 catch-all 로 떨어져 500 이 나가는데,
     * 실제로는 404 라 관리 화면이 "서버 오류" 로 잘못 안내하게 된다.
     */
    @ExceptionHandler({ProductNotFoundException.class,
                       InquiryNotFoundException.class,
                       UsedItemNotFoundException.class,
                       SupportNotFoundException.class,
                       BannerNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND) public Map<String, String> notFound() {return Map.of("code", "NOT_FOUND", "message", "대상을 찾을 수 없습니다.");}
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) public Map<String, String> badRequest(IllegalArgumentException e) {return Map.of("code", "VALIDATION_FAILED", "message", e.getMessage());}
    @ExceptionHandler(InvalidImageException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) public Map<String, String> invalidImage(InvalidImageException e){return Map.of("code", "INVALID_IMAGE", "message", e.getMessage());}
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE) public Map<String , String> tooLarge(){return Map.of("code", "FILE_TOO_LARGE", "message", "파일이 너무 큽니다. 10MB 이하로 올려주세요.");}
}
