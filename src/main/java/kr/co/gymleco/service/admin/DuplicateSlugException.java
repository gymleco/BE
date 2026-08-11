package kr.co.gymleco.service.admin;

public class DuplicateSlugException extends RuntimeException {
    public DuplicateSlugException(String slug) {
        super("이미 사용 중인 주소입니다: " + slug);
    }
}
