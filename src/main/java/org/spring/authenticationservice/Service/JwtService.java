package org.spring.authenticationservice.Service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spring.authenticationservice.model.Role;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private final String secretKey = "dajshdsajhd54343534543534fdfsdf543543jksadjash";

    public String generateToken(String username, List<Role> roles) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);

        // Extract only role names from the list of roles
        List<String> roleNames = roles.stream()
                .map(Role::getName) // Assuming Role has a getName() method
                .toList();
        claims.put("roles", roleNames);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 3)) // 3 minutes expiration
                .signWith(getKey())
                .compact();
    }


    private Key getKey() {

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

   public boolean validateToken(String token){
        try{
            Jwts.parser().verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey))).build().parseSignedClaims(token);
            return true;
        }
        catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }catch (Exception e){
            logger.error("JWT claims string asdasis empty: {}", e.getMessage());
        }
        return  false;

   }


    public Claims getClaimsFromToken(String token) {

        if(!validateToken(token)){
            throw new  JwtException("Invalid or Expired Token");
        }

        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }


    public String generateActivationToken(String userId){
        return Jwts.builder()
                .subject(userId)
                .expiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SignatureAlgorithm.HS256,secretKey)
                .compact();
    }


    public boolean Validate(String jwtToken, UserDetails userDetails) {

        final String username = getClaimsFromToken(jwtToken).getSubject();
        return (username.equals(userDetails.getUsername()) && validateToken(jwtToken));
    }
}
