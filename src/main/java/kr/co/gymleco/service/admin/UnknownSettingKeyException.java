package kr.co.gymleco.service.admin;

public class UnknownSettingKeyException extends RuntimeException{
    public UnknownSettingKeyException(String key){
        super("알 수 없는 설정 항목입니다: " + key);
    }
}
