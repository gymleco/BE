package kr.co.gymleco.security.crypto;

public final class PhoneNumbers {
    private PhoneNumbers(){}
    public static String normalize(String raw){
        if(raw == null){
            return null;
        }
        StringBuilder digits = new StringBuilder(raw.length());
        for(int i = 0; i<raw.length(); i++){
            char c = raw.charAt(i);
            if(c >='0' && c <= '9'){
                digits.append(c);
            }
        }
        String value = digits.toString();
        if(value.startsWith("82")){
            value = "0" + value.substring(2);
        }
        return value;
    }
    public static boolean isPlausible(String normalized){
        return normalized != null
            && normalized.length() >= 9
            && normalized.length() <= 11
            && normalized.startsWith("0");
    }
}
