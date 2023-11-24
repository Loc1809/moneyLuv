package org.rest.Service;

import javax.naming.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.rest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.core.env.Environment;
import org.rest.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final Environment environment;

    public UserService(Environment environment) {
        this.environment = environment;
    }

    public String getCurrentUsername(HttpServletRequest req){
        Claims claims = getClaims(req, environment.getProperty("token.secret"));
        return claims.get("username").toString();
    }

    public User getCurrentUser(HttpServletRequest req, UserRepository userRepository){
//        String username = (getCurrentUsername(req) == null) ? "" : getCurrentUsername(req);
        String username = getCurrentUsername(req);
        return userRepository.getUserByUsername(username);
    }

    public int getCurrentUserId(HttpServletRequest req, UserRepository userRepository) throws AuthenticationException {
        try{
            String username = getCurrentUsername(req);
            return userRepository.findUserByUsernameContains(username).getId();
        } catch (Throwable e){
            throw new AuthenticationException(e.getMessage());
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
