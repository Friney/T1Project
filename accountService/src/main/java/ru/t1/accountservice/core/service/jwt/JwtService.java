package ru.t1.accountservice.core.service.jwt;

import java.time.Duration;
import javax.crypto.SecretKey;
import ru.t1.accountservice.api.dto.jwt.JwtAuthenticationDto;

public interface JwtService {

    JwtAuthenticationDto generateAuthToken(String login);

    JwtAuthenticationDto refreshBaseToken(String login, String refreshToken);

    String getLoginFromToken(String token);

    boolean validateToken(String token);

    String generateJwtToken(String login);

    String generateRefreshToken(String login);

    String generateToken(String login, Duration duration);

    SecretKey getSingInKey();
}
