package org.spring.authenticationservice.Service;

import io.jsonwebtoken.Claims;
import org.spring.authenticationservice.DTO.LoginUserDto;
import org.spring.authenticationservice.DTO.RegisterUserDto;
import org.spring.authenticationservice.model.Role;
import org.spring.authenticationservice.model.TokenType;
import org.spring.authenticationservice.model.User;
import org.spring.authenticationservice.model.VerificationToken;
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

    @Autowired
    private VerificationTokenService verificationTokenService;

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
        userRepository.save(user);

        Map<String, String> emailBody = Map.of(
                "to", user.getEmail(),
                "name", user.getEmail(),  // If user has a getName() method, replace email with user.getName()
                "activationLink", "localhost:8080/activate?token=" + activationToken
        );

        try{
           String mailResponse = emailService.sendEmail("activation",emailBody);
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

    public boolean sendResetPasswordEmail(String email) throws Exception {
        User user = userRepository.findByEmail(email).orElse(null);
        if(user != null){
            String token  = verificationTokenService.generatePasswordResetToken(email);
            // send verification token to email
            Map<String,String> emailBody = Map.of(
                    "to", user.getEmail(),
                    "resetLink", "localhost:8080/reset-password-Token?token=" + token
            );

            try{
                String mailResponse = emailService.sendEmail("resetPassword",emailBody);
                System.out.println(mailResponse);
            }
            catch (Exception e) {
                throw new Exception("Email could not be sent");
            }

        }
        throw new UsernameNotFoundException("User not found with email: " + email);
    }

    public String authenticateUser(LoginUserDto loginUserDto) throws Exception {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginUserDto.getEmail(), loginUserDto.getPassword())
            );

            if (authentication.isAuthenticated()) {
                User user = userRepository.findByEmail(loginUserDto.getEmail())
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                return jwtService.generateToken(user.getEmail(), user.getRoles());
            }
        } catch (BadCredentialsException e) {
            throw new Exception("Invalid email or password");
        }
        throw new Exception("Authentication failed");
    }



    public Claims validateToken(String token) {
        return jwtService.getClaimsFromToken(token);
    }

    public boolean resetPasswordByToken(String token, String newPassword) {
        VerificationToken verificationToken = verificationTokenService.getVerificationToken(token);
        if(verificationTokenService.validateToken(token,TokenType.OTP_VERIFICATION)){
            Optional<User> user = userRepository.findByEmail(verificationToken.getEmail());
            if(user.isPresent()){
                User storedUser = user.get();
                storedUser.setPassword(encoder.encode(newPassword));
                userRepository.save(storedUser);
                return true;
            }
            return false;
        }
        throw new BadCredentialsException("Invalid verification token");
    }
}
