package org.spring.authenticationservice.controller;

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

@RestController
public class authController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;



    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {

        if(userService.findUserByUsername(user.getEmail())){
            return new ResponseEntity<>("Email Already Exists", HttpStatus.CONFLICT);
        }

        userService.saveUser(user);

        return new ResponseEntity<>("User Registered Successfully", HttpStatus.CREATED);
    }

}
