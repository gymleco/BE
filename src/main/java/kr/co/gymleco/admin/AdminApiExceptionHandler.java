package kr.co.gymleco.admin;

import kr.co.gymleco.service.admin.AuthenticationFailedException;
import kr.co.gymleco.service.admin.DuplicateSlugException;
import kr.co.gymleco.service.admin.InquiryNotFoundException;
import kr.co.gymleco.service.admin.ProductNotFoundException;
import kr.co.gymleco.support.image.InvalidImageException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

@RestControllerAdvice(basePackages = "kr.co.gymleco.admin")
public class AdminApiExceptionHandler {
    @ExceptionHandler(AuthenticationFailedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> authFailed(){
        return Map.of("code", "INVALID_CREDENTIALS", "message", "아이디 또는 비밀번호가 올바르지 않습니다.");
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalid(){
        return Map.of("code", "VALIDATION_FAILED", "message", "입력값을 확인해 주세요.");
    }
    @ExceptionHandler(DuplicateSlugException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> duplicateSlug(DuplicateSlugException e){
        return Map.of("code", "DUPLICATE_SLUG", "message", e.getMessage());
    }
    @ExceptionHandler({ProductNotFoundException.class, InquiryNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFound() {
        return Map.of("code", "NOT_FOUND", "message", "대상을 찾을 수 없습니다.");
    }
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> badRequest(IllegalArgumentException e) {
        return Map.of("code", "VALIDATION_FAILED", "message", e.getMessage());
    }
    @ExceptionHandler(InvalidImageException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalidImage(InvalidImageException e){
        return Map.of("code", "INVALID_IMAGE", "message", e.getMessage());
    }
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public Map<String , String> tooLarge(){
        return Map.of("code", "FILE_TOO_LARGE", "message", "파일이 너무 큽니다. 10MB 이하로 올려주세요.");
    }
}
