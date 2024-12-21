package org.spring.authenticationservice.controller;

import io.jsonwebtoken.Claims;
import org.spring.authenticationservice.Service.EmailService;
import org.spring.authenticationservice.Service.JwtService;
import org.spring.authenticationservice.Service.UserService;
import org.spring.authenticationservice.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class authController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private EmailService emailService;


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {

        if (userService.findUserByUsername(user.getEmail())) {
            return new ResponseEntity<>("Email Already Exists", HttpStatus.CONFLICT);
        }

        String activationToken = jwtService.generateActivationToken(user.getEmail());

        userService.saveUser(user);

        // Prepare the email body
        Map<String, String> emailBody = new HashMap<>();
        emailBody.put("to", user.getEmail());
        emailBody.put("name", user.getEmail()); // Assuming `User` has a `getName()` method
        //hosted name domain should be added
        emailBody.put("activationLink", "http//:localhost:8080/activate" + activationToken);


        try {
            String mailResponse = emailService.ActivationEmail(emailBody);
            System.out.println(mailResponse);
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("User registered but email not sent", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("User Registered Successsfully",HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
            );

            if (authentication.isAuthenticated()) {
                String token = jwtService.generateToken(user.getUsername());
                return new ResponseEntity<>(token, HttpStatus.OK);

            }
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>("User Not Found", HttpStatus.NOT_FOUND);
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");

        try {

                Claims claims = jwtService.getClaimsFromToken(token);
                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "user", claims.getSubject()
                ));


        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "valid", false,
                    "error", "Invalid or expired token"
            ));
        }
    }

}
