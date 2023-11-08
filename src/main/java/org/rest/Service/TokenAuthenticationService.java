package org.rest.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Collections.emptyList;

public class TokenAuthenticationService {
//    private static Environment environment;
//
//    public static void setEnvironment(Environment env) {
//        environment = env;
//    }

//    static final long EXPIRATIONTIME = Long.parseLong(environment.getProperty("token.expirationTime")); // 10 days
//    static final String SECRET = environment.getProperty("token.secret");
//    static final String TOKEN_PREFIX = environment.getProperty("token.prefix");
//    static final String HEADER_STRING = environment.getProperty("token.secret");

    static final long EXPIRATIONTIME = 864_000_000; // 10 days
    static final String SECRET = "loctest123";
    static final String TOKEN_PREFIX = "Bearer";
    static final String HEADER_STRING = "Authorization";

    public static void addAuthentication(HttpServletResponse res, String subjects, String username) throws IOException, JSONException {
        String JWT = Jwts.builder()
                .setSubject(subjects)
                .claim("username", username)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
        List<String> response = new ArrayList<>();
        response.add(TOKEN_PREFIX + " " + JWT);
        JSONObject jsonObject = new JSONObject("{\"token\":\"" + TOKEN_PREFIX + " " + JWT + "\", \"role\":\"" + subjects + "\"}");
        res.addHeader(HEADER_STRING, TOKEN_PREFIX + " " + JWT);
        res.setContentType("application/json");
        res.getWriter().write(jsonObject.toString());
    }

    public static Authentication getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(HEADER_STRING);
        if (token != null) {
            // parse the token.
            String user = Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                    .getBody()
                    .getSubject();

            return user != null ?
                    new UsernamePasswordAuthenticationToken(user, null, emptyList()) :
                    null;
        }
        return null;
    }
}
