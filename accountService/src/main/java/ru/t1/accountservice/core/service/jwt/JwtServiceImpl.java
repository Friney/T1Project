package ru.t1.accountservice.core.service.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.t1.accountservice.api.dto.jwt.JwtAuthenticationDto;

@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

    @Value("${t1.jwt.secret}")
    private String jwtSecret;

    @Value("${t1.jwt.token-lifetime}")
    private Duration jwtTokenLifetime;

    @Value("${t1.jwt.refresh-lifetime}")
    private Duration refreshTokenLifetime;

    @Override
    public JwtAuthenticationDto generateAuthToken(String login) {
        return JwtAuthenticationDto.builder()
                .token(generateJwtToken(login))
                .refreshToken(generateRefreshToken(login))
                .build();
    }

    @Override
    public JwtAuthenticationDto refreshBaseToken(String login, String refreshToken) {
        return JwtAuthenticationDto.builder()
                .token(generateJwtToken(login))
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public String getLoginFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSingInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSingInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;
        } catch (Exception e) {
            log.info("{} -> {}", e.getClass(), e.getMessage());
            return false;
        }
    }

    @Override
    public String generateJwtToken(String login) {
        return generateToken(login, jwtTokenLifetime);
    }

    @Override
    public String generateRefreshToken(String login) {
        return generateToken(login, refreshTokenLifetime);
    }

    @Override
    public String generateToken(String login, Duration duration) {
        Date date = Date.from(LocalDateTime.now().plus(duration).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .subject(login)
                .expiration(date)
                .signWith(getSingInKey())
                .compact();
    }

    @Override
    public SecretKey getSingInKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
