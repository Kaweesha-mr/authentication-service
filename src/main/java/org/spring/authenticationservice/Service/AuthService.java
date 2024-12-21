package org.spring.authenticationservice.Service;

import io.jsonwebtoken.Claims;
import org.spring.authenticationservice.model.User;
import org.spring.authenticationservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtService jwtService;

    public void saveUser(User user) {
        user.setPassword(encoder.encode(user.getPassword()));

        userRepository.save(user);
    }

    public Boolean findUserByUsername(String email){
        return userRepository.existsByEmail(email);
    }

    public boolean activateUserAccount(String token) {
        try {

            Claims claims = jwtService.getClaimsFromToken(token);
            String email = claims.getSubject();


            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {

                userRepository.enableUser(email);
                return true;
            } else {

                System.out.println("User not found with email: " + email);
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error activating user: " + e.getMessage());
            return false;
        }
    }



}
