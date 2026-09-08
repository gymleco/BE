package kr.co.gymleco.domain.machine;

public final class SerialNormalizer {
    private SerialNormalizer(){}
    public static String normalize(String raw){
        if(raw == null){
            return "";
        }
        StringBuilder sb = new StringBuilder(raw.length());
        for(char c : raw.toCharArray()){
            if(Character.isLetterOrDigit(c)){
                sb.append(Character.toUpperCase(c));
            }
        }
        return sb.toString();
    }
}
