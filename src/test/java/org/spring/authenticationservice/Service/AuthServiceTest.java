package org.spring.authenticationservice.Service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spring.authenticationservice.DTO.LoginUserDto;
import org.spring.authenticationservice.DTO.RegisterUserDto;
import org.spring.authenticationservice.model.Role;
import org.spring.authenticationservice.model.User;
import org.spring.authenticationservice.repository.RoleRepository;
import org.spring.authenticationservice.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private String validToken;
    private Claims mockClaims;
    private User mockedUser;

    @BeforeEach
    void setUp(){
        validToken = "valid.token.value";
        mockClaims = mock(Claims.class);

        // Prepare data before each test
        mockedUser = new User();
        mockedUser.setEmail("test@example.com");
        mockedUser.setPassword("old_password");  // Set old password
    }

    @Test
    void testValidateToken_Success() {
        // Arrange: Mock the behavior of jwtService to return mockClaims
        when(jwtService.getClaimsFromToken(validToken)).thenReturn(mockClaims);

        // Act: Call the method to test
        Claims claims = authService.validateToken(validToken);

        // Assert: Check that the result is as expected
        assertNotNull(claims); // Ensure claims is not null
        Mockito.verify(jwtService,Mockito.times(1)).getClaimsFromToken(validToken);
    }

    @Test
    void testValidateToken_InvalidToken() {
        // Arrange: Mock the behavior of jwtService to throw an exception for invalid token
        when(jwtService.getClaimsFromToken(validToken)).thenThrow(new RuntimeException("Invalid token"));

        // Act & Assert: Check that calling validateToken throws the expected exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.validateToken(validToken);
        });

        assertEquals("Invalid token", exception.getMessage()); // Ensure the exception message is as expected
    }

    @Test
    void testChangePassword_Success() {
        // Arrange: Mock the repository and the password encoder
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockedUser));
        when(passwordEncoder.matches("old_password", mockedUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("new_password")).thenReturn("encoded_new_password");  // Correct the encoding for new password

        // Act: Call the method to test
        boolean result = authService.changeAccountPassword("test@example.com", "old_password", "new_password");

        // Assert: Verify password change was successful
        assertTrue(result);  // Ensure the result is true
        assertEquals("encoded_new_password", mockedUser.getPassword());  // Check if the new password was encoded and set correctly
        verify(userRepository, times(1)).save(mockedUser);  // Ensure the user repository's save method was called once
    }

    @Test
    void testChangeAccountPassword_UserNotFound() {
        // Arrange: Mock the repository to return empty for a non-existing user
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // Act: Call the method to test
        boolean result = authService.changeAccountPassword("test@example.com", "old_password", "new_password");

        // Assert: Verify password change failed due to user not found
        assertFalse(result);  // Ensure the result is false (user not found)
    }

    @Test
    void testChangeAccountPassword_OldPasswordDoesNotMatch() {
        // Arrange: Mock the repository to return the mock user
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockedUser));
        when(passwordEncoder.matches("old_password", mockedUser.getPassword())).thenReturn(false);  // Simulate incorrect password

        // Act: Call the method to test
        boolean result = authService.changeAccountPassword("test@example.com", "old_password", "new_password");

        // Assert: Verify password change failed due to incorrect old password
        assertFalse(result);  // Ensure the result is false (password mismatch)
    }

    // New test for authenticateUser method

    @Test
    void testAuthenticateUser_Success() throws Exception {
        // Arrange: Mock successful authentication
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        // Mock the repository to return a valid user
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockedUser));

        // Mock the JWT token generation
        String mockToken = "mock_token";
        when(jwtService.generateToken(mockedUser.getEmail(), mockedUser.getRoles())).thenReturn(mockToken);

        // Act: Call the method under test
        String token = authService.authenticateUser(new LoginUserDto("test@example.com", "password"));

        // Assert: Verify the token is returned
        assertNotNull(token);
        assertEquals(mockToken, token);

        // Verify interactions
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(jwtService, times(1)).generateToken(mockedUser.getEmail(), mockedUser.getRoles());
    }

    @Test
    void testAuthenticateUser_InvalidCredentials() {
        // Arrange: Mock authentication failure
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        // Act & Assert: Check if exception is thrown
        Exception exception = assertThrows(Exception.class, () -> {
            authService.authenticateUser(new LoginUserDto("test@example.com", "wrong_password"));
        });
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void testAuthenticateUser_UserNotFound() throws Exception {
        // Arrange: Mock successful authentication
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        // Mock userRepository to return empty for non-existing user
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // Act & Assert: Check if exception is thrown for user not found
        Exception exception = assertThrows(Exception.class, () -> {
            authService.authenticateUser(new LoginUserDto("test@example.com", "password"));
        });
        assertEquals("User not found", exception.getMessage());
    }
//
//    @Test
//    void testRegisterUser_Success() throws Exception {
//        // Arrange: Mock the behavior of roleRepository and jwtService
//        Role userRole = new Role();
//        userRole.setName("USER");
//
//        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
//        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);  // User does not exist
//        when(jwtService.generateActivationToken("test@example.com")).thenReturn("activationToken123");
//
//        // Mock the email service to return success
//        when(emailService.ActivationEmail(anyMap())).thenReturn("Email sent");
//
//        RegisterUserDto registerUserDto = new RegisterUserDto();
//        registerUserDto.setEmail("test@example.com");
//        registerUserDto.setPassword("password123");
//
//        // Act: Call the method under test
//        authService.RegisterUser(registerUserDto);
//
//        // Assert: Verify the user was saved and email sent
//        verify(userRepository, times(1)).save(any(User.class));  // Ensure save is called
//        verify(emailService, times(1)).ActivationEmail(anyMap());  // Ensure email was sent
//    }
//
//    @Test
//    void testRegisterUser_UserAlreadyExists() {
//        // Arrange: Mock the repository to return true for existing user
//        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
//
//        RegisterUserDto registerUserDto = new RegisterUserDto();
//        registerUserDto.setEmail("test@example.com");
//        registerUserDto.setPassword("password123");
//
//        // Act & Assert: Ensure exception is thrown
//        Exception exception = assertThrows(Exception.class, () -> {
//            authService.RegisterUser(registerUserDto);
//        });
//
//        assertEquals("User already exists", exception.getMessage());
//    }
//
//    @Test
//    void testRegisterUser_RoleNotFound() throws Exception {
//        // Arrange: Mock the roleRepository to return empty for role
//        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
//
//        RegisterUserDto registerUserDto = new RegisterUserDto();
//        registerUserDto.setEmail("test@example.com");
//        registerUserDto.setPassword("password123");
//
//        // Act & Assert: Ensure exception is thrown for role not found
//        Exception exception = assertThrows(RuntimeException.class, () -> {
//            authService.RegisterUser(registerUserDto);
//        });
//
//        assertEquals("Role USER not found", exception.getMessage());
//    }

}
