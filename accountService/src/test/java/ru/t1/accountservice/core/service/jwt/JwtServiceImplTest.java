package ru.t1.accountservice.core.service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.t1.accountservice.api.dto.jwt.JwtAuthenticationDto;
import ru.t1.accountservice.core.service.jwt.version.JwtVersionService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    @Mock
    private JwtVersionService jwtVersionService;
    @InjectMocks
    private JwtServiceImpl jwtService;

    private final String testLogin = "testUser";
    private final Duration tokenLifetime = Duration.ofMinutes(30);
    private final Duration refreshLifetime = Duration.ofDays(30);
    private final Long tokenVersion = 1L;

    @BeforeEach
    void setUp() {
        String jwtSecret = "veryLongSecretKeyForTestingPurposesOnly12345";
        ReflectionTestUtils.setField(jwtService, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtService, "jwtTokenLifetime", tokenLifetime);
        ReflectionTestUtils.setField(jwtService, "refreshTokenLifetime", refreshLifetime);
    }

    private String generateValidToken(Duration lifetime) {
        Date expirationDate = Date.from(LocalDateTime.now()
                .plus(lifetime)
                .atZone(ZoneId.systemDefault())
                .toInstant());

        return Jwts.builder()
                .subject("testUser")
                .claim("version", tokenVersion)
                .expiration(expirationDate)
                .signWith(jwtService.getSingInKey())
                .compact();
    }

    private String generateExpiredToken() {
        Date expirationDate = Date.from(LocalDateTime.now()
                .minus(Duration.ofDays(1))
                .atZone(ZoneId.systemDefault())
                .toInstant());

        return Jwts.builder()
                .subject("testUser")
                .claim("version", tokenVersion)
                .expiration(expirationDate)
                .signWith(jwtService.getSingInKey())
                .compact();
    }

    @Test
    void generateAuthTokenSuccess() {
        when(jwtVersionService.getCurrentVersion(testLogin)).thenReturn(tokenVersion);

        JwtAuthenticationDto result = jwtService.generateAuthToken(testLogin);

        assertNotNull(result);
        assertNotNull(result.token());
        assertNotNull(result.refreshToken());

        // Verify token claims
        Claims tokenClaims = Jwts.parser()
                .verifyWith(jwtService.getSingInKey())
                .build()
                .parseSignedClaims(result.token())
                .getPayload();

        assertEquals(testLogin, tokenClaims.getSubject());
        assertEquals(tokenVersion, tokenClaims.get("version", Long.class));
        assertTrue(tokenClaims.getExpiration().after(new Date()));

        // Verify refresh token claims
        Claims refreshTokenClaims = Jwts.parser()
                .verifyWith(jwtService.getSingInKey())
                .build()
                .parseSignedClaims(result.refreshToken())
                .getPayload();

        assertEquals(testLogin, refreshTokenClaims.getSubject());
        assertEquals(tokenVersion, refreshTokenClaims.get("version", Long.class));
        assertTrue(refreshTokenClaims.getExpiration().after(new Date()));

        verify(jwtVersionService, times(2)).getCurrentVersion(testLogin);
    }

    @Test
    void refreshBaseTokenSuccess() {
        String refreshToken = generateValidToken(refreshLifetime);
        when(jwtVersionService.getCurrentVersion(testLogin)).thenReturn(tokenVersion);

        JwtAuthenticationDto result = jwtService.refreshBaseToken(testLogin, refreshToken);

        assertNotNull(result);
        assertNotNull(result.token());
        assertEquals(refreshToken, result.refreshToken());

        // Verify new token claims
        Claims tokenClaims = Jwts.parser()
                .verifyWith(jwtService.getSingInKey())
                .build()
                .parseSignedClaims(result.token())
                .getPayload();

        assertEquals(testLogin, tokenClaims.getSubject());
        assertEquals(tokenVersion, tokenClaims.get("version", Long.class));
        assertTrue(tokenClaims.getExpiration().after(new Date()));

        verify(jwtVersionService).getCurrentVersion(testLogin);
    }

    @Test
    void getLoginFromTokenSuccess() {
        String token = generateValidToken(tokenLifetime);

        String result = jwtService.getLoginFromToken(token);

        assertEquals(testLogin, result);
    }

    @Test
    void getLoginFromTokenInvalidToken() {
        String invalidToken = "invalidToken";

        assertThrows(RuntimeException.class,
                () -> jwtService.getLoginFromToken(invalidToken));
    }

    @Test
    void validateTokenSuccess() {
        String token = generateValidToken(tokenLifetime);
        when(jwtVersionService.isValidVersion(testLogin, tokenVersion)).thenReturn(true);

        boolean result = jwtService.validateToken(token);

        assertTrue(result);
        verify(jwtVersionService).isValidVersion(testLogin, tokenVersion);
    }

    @Test
    void validateTokenInvalidVersion() {
        String token = generateValidToken(tokenLifetime);
        when(jwtVersionService.isValidVersion(testLogin, tokenVersion)).thenReturn(false);

        boolean result = jwtService.validateToken(token);

        assertFalse(result);
        verify(jwtVersionService).isValidVersion(testLogin, tokenVersion);
    }

    @Test
    void validateTokenExpired() {
        String token = generateExpiredToken();

        boolean result = jwtService.validateToken(token);

        assertFalse(result);
        verify(jwtVersionService, never()).isValidVersion(any(), any());
    }

    @Test
    void validateTokenInvalidFormat() {
        String invalidToken = "invalidToken";

        boolean result = jwtService.validateToken(invalidToken);

        assertFalse(result);
        verify(jwtVersionService, never()).isValidVersion(any(), any());
    }

    @Test
    void validateTokenNull() {
        boolean result = jwtService.validateToken(null);

        assertFalse(result);
        verify(jwtVersionService, never()).isValidVersion(any(), any());
    }

    @Test
    void generateJwtTokenSuccess() {
        when(jwtVersionService.getCurrentVersion(testLogin)).thenReturn(tokenVersion);

        String token = jwtService.generateJwtToken(testLogin);

        assertNotNull(token);

        Claims claims = Jwts.parser()
                .verifyWith(jwtService.getSingInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(testLogin, claims.getSubject());
        assertEquals(tokenVersion, claims.get("version", Long.class));
        assertTrue(claims.getExpiration().after(new Date()));
        assertTrue(claims.getExpiration().before(Date.from(LocalDateTime.now()
                .plus(tokenLifetime)
                .atZone(ZoneId.systemDefault())
                .toInstant())));

        verify(jwtVersionService).getCurrentVersion(testLogin);
    }

    @Test
    void generateRefreshTokenSuccess() {
        when(jwtVersionService.getCurrentVersion(testLogin)).thenReturn(tokenVersion);

        String token = jwtService.generateRefreshToken(testLogin);

        assertNotNull(token);

        Claims claims = Jwts.parser()
                .verifyWith(jwtService.getSingInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(testLogin, claims.getSubject());
        assertEquals(tokenVersion, claims.get("version", Long.class));
        assertTrue(claims.getExpiration().after(new Date()));
        assertTrue(claims.getExpiration().before(Date.from(LocalDateTime.now()
                .plus(refreshLifetime)
                .atZone(ZoneId.systemDefault())
                .toInstant())));

        verify(jwtVersionService).getCurrentVersion(testLogin);
    }

    @Test
    void generateTokenSuccess() {
        Duration customDuration = Duration.ofHours(2);
        when(jwtVersionService.getCurrentVersion(testLogin)).thenReturn(tokenVersion);

        String token = jwtService.generateToken(testLogin, customDuration);

        assertNotNull(token);

        Claims claims = Jwts.parser()
                .verifyWith(jwtService.getSingInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(testLogin, claims.getSubject());
        assertEquals(tokenVersion, claims.get("version", Long.class));
        assertTrue(claims.getExpiration().after(new Date()));
        assertTrue(claims.getExpiration().before(Date.from(LocalDateTime.now()
                .plus(customDuration)
                .atZone(ZoneId.systemDefault())
                .toInstant())));

        verify(jwtVersionService).getCurrentVersion(testLogin);
    }

    @Test
    void getSignInKeySuccess() {
        SecretKey key = jwtService.getSingInKey();

        assertNotNull(key);
        assertEquals("HmacSHA256", key.getAlgorithm());
    }
}
