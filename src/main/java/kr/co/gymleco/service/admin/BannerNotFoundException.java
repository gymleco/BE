package kr.co.gymleco.service.admin;

public class BannerNotFoundException extends RuntimeException {
    public BannerNotFoundException(Long id) {
        super("배너를 찾을 수 없습니다: " + id);
    }
}
