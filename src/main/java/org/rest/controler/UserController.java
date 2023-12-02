package org.rest.controler;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.hibernate.Session;
import org.rest.Service.UserService;
import org.rest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

import org.rest.model.User;
import org.springframework.web.client.RestTemplate;

@CrossOrigin
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;
    private final Environment environment;

    public UserController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/")
    public ResponseEntity<Object> getAllUsers() {
        return new ResponseEntity<>(userRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/current")
    public ResponseEntity<Object> getCurrentUser(HttpServletRequest req) {
        try {
            return new ResponseEntity<>(new UserService(environment).getCurrentUser(req, userRepository), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable("id") Integer id) {
        Optional<User> userFound = userRepository.findById(id);
        return userFound.<ResponseEntity<Object>>map(user ->
                new ResponseEntity<>(user, HttpStatus.OK)).orElseGet(() ->
                new ResponseEntity<>("User with id " + id + " not found!", HttpStatus.NOT_FOUND));
    }


    @PutMapping("/upgrade/{id}")
    public ResponseEntity<Object> makeUserAdmin(@PathVariable String id, HttpServletRequest req) {
        try {
            Claims claims = getClaims(req, environment.getProperty("token.secret"));
            String subject = claims.getSubject();
            if (subject.equalsIgnoreCase("[ROLE_admin]")) {
                Optional<User> userFound = userRepository.findById(Integer.valueOf(id));
                if (userFound.isPresent()) {
                    User currentUser = userFound.get();
                    currentUser.setRole("[ROLE_admin]");
                    return new ResponseEntity<>(userRepository.save(currentUser), HttpStatus.OK);
                } else
                    return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>("Only admin can do it", HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            return new ResponseEntity<>(e.getCause().getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createNewUser(@RequestBody User userModel, HttpServletRequest req) {
        try {
                Optional<User> userFound = userRepository.findUserByUsernameOrEmail(userModel.getUsername(), userModel.getEmail());
                if (userFound.isPresent())
                    return new ResponseEntity<>("This username has been taken!", HttpStatus.BAD_REQUEST);
                User user = new User(userModel.getUsername(), new BCryptPasswordEncoder().encode(userModel.password()),
                        userModel.getPhoneNumber(), userModel.getEmail(), userModel.getName(), userModel.getIdentifyCode(),
                        userModel.getDateOfBirth(), "[ROLE_user]");
                user.setEnabled(true);
                User response = userRepository.save(user);
                Session session = entityManager.unwrap(Session.class);
                session.evict(response);
                return new ResponseEntity<>(response, HttpStatus.OK);
//            } else
//                return new ResponseEntity<>("Only [admin] can do it", HttpStatus.UNAUTHORIZED);
        } catch (DataIntegrityViolationException dupplicate){
            return new ResponseEntity<>("Database error, please try again", HttpStatus.CONFLICT);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getCause().getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
//    public

    @PutMapping("/user/lock/{id}")
//    @PreAuthorize("hasRole('ROLE_admin')")
    public ResponseEntity<Object> lockUser(HttpServletRequest req, @PathVariable("id") Integer id, @RequestParam("action") String action) {
        try {
//            Enable/Lock
            boolean status = action.equals("enable");
            Optional<User> userFound = userRepository.findById(id);
            if (userFound.isPresent()) {
                User thisUser = userFound.get();
                thisUser.setEnabled(status);
                return new ResponseEntity<>(userRepository.save(thisUser), HttpStatus.OK);
            }
            return new ResponseEntity<>("User " + id + " not found!", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getCause().getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<String> getUserRoles(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Get the roles of the authenticated user
        List<String> roles = authentication.getAuthorities()
            .stream()
            .map(Object::toString)
            .collect(Collectors.toList());
        return roles;
    }

    public Claims getClaims(HttpServletRequest req, String signingKey) {
        String token = req.getHeader("Authorization").replace("Bearer", "").trim();
        return Jwts.parser()
                .setSigningKey(signingKey)
                .parseClaimsJws(token)
                .getBody();
    }
}
