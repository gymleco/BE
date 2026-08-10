package kr.co.gymleco.service.inquiry;

public class RateLimitExceededException extends RuntimeException{
    public RateLimitExceededException(){
        super("요청 한도 초과하였습니다.");
    }
}
