package bookShop.service;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

@Component
public class JwtUtil {
    private final String jwtSecret = "your_secret_key"; // можно заменить на более сложный
    private final long jwtExpirationMs = 24 * 60 * 60 * 1000; // сутки

    public String generateToken(bookShop.model.AppUserDetails userDetails) {
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("role", userDetails.getRole().name());
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String extractRole(String token) {
        return (String) Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .get("role");
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        Date expiration = Jwts.parser().setSigningKey(jwtSecret)
                .parseClaimsJws(token).getBody().getExpiration();
        return expiration.before(new Date());
    }
}