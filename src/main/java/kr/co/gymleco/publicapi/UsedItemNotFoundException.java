package kr.co.gymleco.publicapi;

public class UsedItemNotFoundException extends RuntimeException{
    public UsedItemNotFoundException(String slug){
        super("중고 매물을 찾을 수 없습니다:" + slug);
    }
}
