package kr.co.gymleco.service.admin;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long id) {
        super("제품을 찾을 수 없습니다" + (id == null ? "" : ": " + id));
    }
}
