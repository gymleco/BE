package kr.co.gymleco.security.jwt;
import kr.co.gymleco.config.GymlecoProperties;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.stereotype.Service;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {
    private static final String ISSUER = "gymleco-api";
    private static final String CLAIM_ROLE = "role";
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final GymlecoProperties.Auth auth;
    public JwtTokenService(GymlecoProperties properties){
        this.auth = properties.auth();
        byte[] secret = auth.jwtSecret().getBytes(StandardCharsets.UTF_8);
        SecretKeySpec key = new SecretKeySpec(secret, "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        this.decoder = NimbusJwtDecoder.withSecretKey(key)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();
    }
    public String issueAccessToken(Long adminId, String username, String role){
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(ISSUER)
            .issuedAt(now)
            .expiresAt(now.plus(auth.accessTtl()))
            .subject(String.valueOf(adminId))
            .claim("username", username)
            .claim(CLAIM_ROLE, role)
            .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
    public Jwt verify(String token) throws JwtException{
        return decoder.decode(token);
    }
    public JwtDecoder decoder(){
        return decoder;
    }
    public static List<String> authoritesOf(Jwt jwt){
        String role = jwt.getClaimAsString(CLAIM_ROLE);
        return role == null ? List.of() : List.of("ROLE_" + role);
    }
}
