package kr.co.gymleco.domain.inquiry;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class ReferenceNumbers {
    private static final char[] ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ".toCharArray();
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyMMdd").withZone(ZoneId.of("Asia/Seoul"));
    private static final SecureRandom RANDOM = new SecureRandom();
    private ReferenceNumbers(){}
    public static String next(Instant now){
        StringBuilder sb = new StringBuilder("GL-").append(DAY.format(now)).append('-');
        for(int i = 0; i < 4; i++){
            sb.append(ALPHABET[RANDOM.nextInt(ALPHABET.length)]);
        }
        return sb.toString();
    }
}
