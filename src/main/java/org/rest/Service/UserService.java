package org.rest.Service;

import javax.servlet.http.HttpServletRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.core.env.Environment;

public class UserService {
    private final Environment environment;

    public UserService(Environment environment) {
        this.environment = environment;
    }

    public String getCurrentUsername(HttpServletRequest req){
        Claims claims = getClaims(req, environment.getProperty("token.secret"));
        return claims.get("username").toString();
    }

    public int getCurrentUserId(HttpServletRequest req){
        try{
            Claims claims = getClaims(req, environment.getProperty("token.secret"));
            return Integer.parseInt(claims.get("userid").toString());
        } catch (Exception e){
            return 1;
        }
    }

    public Claims getClaims(HttpServletRequest req, String signingKey) {
        String token = req.getHeader("Authorization").replace("Bearer", "").trim();
        return Jwts.parser()
                .setSigningKey(signingKey)
                .parseClaimsJws(token)
                .getBody();
    }
}
