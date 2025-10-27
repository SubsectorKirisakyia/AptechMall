package com.aptech.aptechMall.service.authentication;

import com.aptech.aptechMall.entity.User;
import com.aptech.aptechMall.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Autowired
    private UserRepository userRepository;

    @Value("${jwt.secret-key}")
    private String secretKey;

    private static final int ACCESS_TOKEN_TTL = 5 * 60 * 1000;
    protected static final int REFRESH_TOKEN_TTL = 8 * 24 * 60 * 60;

//   public JwtService(){
//        try {
//            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
//            SecretKey sk = keyGen.generateKey();
//            secretKey = Base64.getEncoder().encodeToString(sk.getEncoded());
//            System.out.println("Secret: "+ secretKey);
//        } catch (Exception e) {
//            System.err.println("JWTService class failed to initialize: " +e.getMessage());
//            throw new RuntimeException("JwtService class failed to initialize secret key");
//        }
//    } // Uncomment in case new key need to be regenerated and manually assigned to ${jwt.secret-key}

    public String generateToken(String username, String tokenType) {
        validateTokenType(tokenType);

        User user = userRepository.existsByUsername(username)
                ? userRepository.findByUsername(username).orElseThrow()
                : userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("No email found for Token Generation"));

        return buildToken(user, tokenType);
    }

    public String generateToken(User user, String tokenType) {
        validateTokenType(tokenType);
        return buildToken(user, tokenType);
    }

    private void validateTokenType(String tokenType) {
        if (!"access_token".equals(tokenType) && !"refresh_token".equals(tokenType)) {
            throw new IllegalArgumentException("Token type " + tokenType + " not supported");
        }
    }

    private Map<String, Object> extractClaims(User user, String tokenType) {
        Map<String, Object> claims = new HashMap<>();

        claims.put("id", user.getId());
        claims.put("role", user.getRole().name());
        claims.put("type", tokenType);
        claims.put("email", user.getEmail());
        claims.put("fullname", user.getFullName());
        claims.put("status", user.getStatus().name());

        return claims;
    }

    private String buildToken(User user, String tokenType) {
        Map<String, Object> claims = extractClaims(user, tokenType);
        String subject = user.getUsername() != null ? user.getUsername() : user.getEmail();
        long expirationTime = System.currentTimeMillis() +
                ("refresh_token".equals(tokenType) ? REFRESH_TOKEN_TTL : ACCESS_TOKEN_TTL);

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(expirationTime))
                .and()
                .signWith(getKey())
                .compact();
    }

    private SecretKey getKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public boolean validateToken(String jwtToken) {
        return extractExpiration(jwtToken).after(new Date());
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
