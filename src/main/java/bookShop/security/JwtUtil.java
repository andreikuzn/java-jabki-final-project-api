package bookShop.security;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {
    private final String jwtSecret = "your_secret_key";
    private final long jwtExpirationMs = 24 * 60 * 60 * 1000; // сутки

    public String generateToken(bookShop.model.AppUserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userDetails.getRole().name());
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
        log.info("JWT токен сгенерирован для пользователя [{}], роль [{}]", userDetails.getUsername(), userDetails.getRole());
        return token;
    }

    public String extractRole(String token) {
        try {
            String role = (String) Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody()
                    .get("role");
            log.debug("Роль [{}] извлечена из JWT токена", role);
            return role;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Ошибка при извлечении роли из токена: {}", e.getMessage());
            throw e;
        }
    }

    public String extractUsername(String token) {
        try {
            String username = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            log.debug("Имя пользователя [{}] извлечено из JWT токена", username);
            return username;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Ошибка при извлечении имени пользователя из токена: {}", e.getMessage());
            throw e;
        }
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean valid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
            if (valid) {
                log.info("JWT токен успешно валидирован для пользователя [{}]", username);
            } else {
                log.warn("JWT токен не валиден для пользователя [{}]", userDetails.getUsername());
            }
            return valid;
        } catch (ExpiredJwtException e) {
            log.warn("JWT токен истёк: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Ошибка валидации JWT токена: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            boolean expired = expiration.before(new Date());
            if (expired) {
                log.debug("JWT токен истёк (expiration: {})", expiration);
            }
            return expired;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Ошибка при проверке истечения срока действия токена: {}", e.getMessage());
            return true;
        }
    }
}