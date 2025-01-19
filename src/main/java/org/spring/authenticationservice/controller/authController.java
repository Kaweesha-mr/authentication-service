package org.spring.authenticationservice.controller;

import io.jsonwebtoken.Claims;
import org.spring.authenticationservice.DTO.LoginUserDto;
import org.spring.authenticationservice.DTO.RegisterUserDto;
import org.spring.authenticationservice.Service.AuthService;
import org.spring.authenticationservice.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class authController {

    @Autowired
    private AuthService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterUserDto registerUserDto) {

        try {
            userService.RegisterUser(registerUserDto);
            return new ResponseEntity<>("User Registered Successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginUserDto loginUserDto) {
        try {
            String token  = userService.authenticateUser(loginUserDto);
            return new ResponseEntity<>(token, HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> request) {
        try {

            Claims claims = userService.validateToken(request.get("token"));
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

        if (userService.activateUserAccount(token)) {
            return new ResponseEntity<>("User Activated", HttpStatus.OK);
        }
        return new ResponseEntity<>("User Not Found", HttpStatus.NOT_FOUND);

    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        String newPassword = request.get("newPassword");

        if (!userService.changeAccountPassword(email, password, newPassword)) {
            return new ResponseEntity<>("Password reset failed: Incorrect email or password", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>("Password reset successful", HttpStatus.OK);

    }


}
