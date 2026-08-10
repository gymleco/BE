package kr.co.gymleco.security.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

public class CookieBearerTokenResolver implements BearerTokenResolver{
    public static final String ACCESS_TOKEN_COOKIE = "gl_at";
    @Override
    public String resolve(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        if(cookies == null){
            return null;
        }
        for(Cookie cookie : cookies){
            if(ACCESS_TOKEN_COOKIE.equals(cookie.getName())){
                String value = cookie.getValue();
                return(value == null || value.isBlank()) ? null : value;
            }
        }
        return null;
    }
}
