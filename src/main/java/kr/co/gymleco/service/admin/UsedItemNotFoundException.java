package kr.co.gymleco.service.admin;

public class UsedItemNotFoundException extends RuntimeException {
    public UsedItemNotFoundException(Long id) {
        super("중고 매물을 찾을 수 없습니다: " + id);
    }
}
