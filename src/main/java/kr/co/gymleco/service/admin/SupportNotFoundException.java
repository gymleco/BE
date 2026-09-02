package kr.co.gymleco.service.admin;

public class SupportNotFoundException extends RuntimeException {
    public SupportNotFoundException(String what, Long id) {
        super(what + " 을(를) 찾을 수 없습니다: " + id);
    }
}
