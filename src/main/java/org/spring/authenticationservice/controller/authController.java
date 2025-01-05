package org.spring.authenticationservice.controller;

import io.jsonwebtoken.Claims;
import org.spring.authenticationservice.Service.EmailService;
import org.spring.authenticationservice.Service.JwtService;
import org.spring.authenticationservice.Service.AuthService;
import org.spring.authenticationservice.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class authController {

    @Autowired
    private AuthService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private AuthService authService;


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {

        if (userService.findUserByUsername(user.getEmail())) {
            return new ResponseEntity<>("Email Already Exists", HttpStatus.CONFLICT);
        }

        String activationToken = jwtService.generateActivationToken(user.getEmail());



        // Prepare the email body
        Map<String, String> emailBody = new HashMap<>();
        emailBody.put("to", user.getEmail());
        emailBody.put("name", user.getEmail()); // Assuming `User` has a `getName()` method
        //hosted name domain should be added
        emailBody.put("activationLink", "localhost:8080/activate?token=" + activationToken);


        try {
            String mailResponse = emailService.ActivationEmail(emailBody);
            userService.saveUser(user);
            System.out.println(mailResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("User registered but email not sent", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("User Registered Successsfully", HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        try {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
            );

            if (authentication.isAuthenticated()) {
                String token = jwtService.generateToken(user.getEmail());
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

    @GetMapping("/activate")
    public ResponseEntity<?> activate(@RequestParam String token) {

        if (authService.activateUserAccount(token)) {
            return new ResponseEntity<>("User Activated", HttpStatus.OK);
        }

        return new ResponseEntity<>("User Not Found", HttpStatus.NOT_FOUND);


    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        String newPassword = request.get("newPassword");

        if (!authService.changeAccountPassword(email, password, newPassword)) {
            return new ResponseEntity<>("Password reset failed: Incorrect email or password", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>("Password reset successful", HttpStatus.OK);

    }


}
