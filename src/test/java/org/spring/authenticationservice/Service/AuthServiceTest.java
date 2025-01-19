package org.spring.authenticationservice.Service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.spring.authenticationservice.model.User;
import org.spring.authenticationservice.repository.UserRepository;
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

    @InjectMocks
    private AuthService authService;

    private String validToken;
    private Claims mockClaims;

    private User mockedUser;

    @BeforeEach
    void  setUp(){
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




}
