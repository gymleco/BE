package kr.co.gymleco.service.admin;

public class AuthenticationFailedException extends RuntimeException {
    public AuthenticationFailedException() {
        super("아이디 또는 비밀번호가 올바르지 않습니다.");
    }
}
