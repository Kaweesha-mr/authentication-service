package org.spring.authenticationservice.Service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private final String secretKey = "dajshdsajhd54343534543534fdfsdf543543jksadjash";

    public String generateToken(String username) {

        Map<String,Object> claims = new HashMap<>();


        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+ 1000*60*3))
                .signWith(getKey()).compact();

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
}
