package org.spring.authenticationservice.Service;

import org.spring.authenticationservice.model.VerificationToken;
import org.spring.authenticationservice.model.TokenType;
import org.spring.authenticationservice.repository.VerificationTokenRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.Random;

@Service
public class VerificationTokenService {

    private final VerificationTokenRepository tokenRepository;

    public VerificationTokenService(VerificationTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    // Generate a new Password Reset Token
    public String generatePasswordResetToken(String email) {
        String token = UUID.randomUUID().toString(); // Random token
        saveToken(email, token, TokenType.PASSWORD_RESET, LocalDateTime.now().plusHours(1));
        return token;
    }

    // Generate a new OTP
    public String generateOtp(String email) {
        String otp = String.valueOf(new Random().nextInt(900000) + 100000); // 6-digit OTP
        saveToken(email, otp, TokenType.OTP_VERIFICATION, LocalDateTime.now().plusMinutes(5));
        return otp;
    }

    // Save the token to the database
    private void saveToken(String email, String token, TokenType tokenType, LocalDateTime expiration) {
        tokenRepository.deleteByEmailAndTokenType(email, tokenType); // Remove old token
        VerificationToken verificationToken = new VerificationToken(token, email, expiration, tokenType);
        tokenRepository.save(verificationToken);
    }

    // Validate a token
    public boolean validateToken(String token, TokenType tokenType) {
        Optional<VerificationToken> optionalToken = tokenRepository.findByTokenAndTokenType(token, tokenType);
        return optionalToken.isPresent() && optionalToken.get().getExpirationDate().isAfter(LocalDateTime.now());
    }

    public VerificationToken getVerificationToken(String token) {
        return tokenRepository.findByToken(token);
    }
}
