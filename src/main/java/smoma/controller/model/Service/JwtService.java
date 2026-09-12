package smoma.controller.model.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * Issues and validates the signed JWT that proves a request really comes from whoever it claims
 * to be. Before this existed, every controller trusted the client-supplied X-User-Email header
 * with no proof at all — anyone could set it to any identity and every AccessPolicy role check
 * downstream would trust it blindly. A token from here can only have been produced by a real,
 * successful login (see AuthController), so JwtAuthFilter can require one to match the claimed
 * identity before any mutating request is allowed through.
 */
@Component
public class JwtService {

    private final SecretKey key;
    private final long expirationMillis;

    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expirationMillis) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(String subject, Map<String, Object> claims) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(subject)
                .addClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Returns the token's subject if it is validly signed and not expired, or null otherwise. */
    public String validateAndGetSubject(String token) {
        if (token == null || token.isBlank()) return null;
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
