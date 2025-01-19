package org.spring.authenticationservice.Service;

import io.jsonwebtoken.Claims;
import org.spring.authenticationservice.DTO.LoginUserDto;
import org.spring.authenticationservice.DTO.RegisterUserDto;
import org.spring.authenticationservice.model.Role;
import org.spring.authenticationservice.model.User;
import org.spring.authenticationservice.repository.RoleRepository;
import org.spring.authenticationservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RoleRepository roleRepository;

    public void RegisterUser(RegisterUserDto registerUserDto) throws Exception {
        User user = new User();
        user.setEmail(registerUserDto.getEmail());
        user.setPassword(encoder.encode(registerUserDto.getPassword()));
        // Assign default role USER
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role USER not found"));
        user.getRoles().add(userRole);

        if (findUserByUsername(user.getEmail())) {
            throw new Exception("User already exists");
        }

        String activationToken = jwtService.generateActivationToken(user.getEmail());

        // Prepare the email body
        Map<String, String> emailBody = new HashMap<>();
        emailBody.put("to", user.getEmail());
        emailBody.put("name", user.getEmail()); // Assuming `User` has a `getName()` method

        //hosted name domain should be added
        emailBody.put("activationLink", "localhost:8080/activate?token=" + activationToken);
        userRepository.save(user);

        try{
           String mailResponse = emailService.ActivationEmail(emailBody);
           System.out.println(mailResponse);
       }
       catch (Exception e) {
           throw new Exception("Email could not be sent");
       }
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


    public boolean changeAccountPassword(String email, String password, String newPassword){



        User user = userRepository.findByEmail(email).orElse(null);
        if(user!= null && encoder.matches(password, user.getPassword())){
            user.setPassword(encoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public String authenticateUser(LoginUserDto loginUserDto) throws Exception {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginUserDto.getEmail(), loginUserDto.getPassword())
            );

            if (authentication.isAuthenticated()) {
                User user = userRepository.findByEmail(loginUserDto.getEmail())
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                String token = jwtService.generateToken(user.getEmail(), user.getRoles());

                return token;
            }
        } catch (BadCredentialsException e) {
            throw new Exception("Invalid email or password");
        }
        throw new Exception("Authentication failed");
    }



    public Claims validateToken(String token) {
        return jwtService.getClaimsFromToken(token);
    }
}
