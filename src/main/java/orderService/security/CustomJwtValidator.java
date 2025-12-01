package orderService.security;

import UserService.dto.TokenValidationResponse;
import UserService.exception.TokenValidationException;
import io.jsonwebtoken.*;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

@Component
public class CustomJwtValidator {

    private static final Logger logger = LoggerFactory.getLogger(CustomJwtValidator.class);
    private final CustomKeyProvider customKeyProvider;
    @Getter
    private final JwtParser jwtParser;

    @Value("${jwt.remote-validation-url}")
    private String remoteValidationUrl;

    public CustomJwtValidator(CustomKeyProvider customKeyProvider) {
        this.customKeyProvider = customKeyProvider;
        try {
            this.jwtParser = Jwts
                    .parser()
                    .verifyWith(customKeyProvider.getPublicKey())
                    .build();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new TokenValidationException(e.getMessage());
        }
    }

    public boolean validateToken(String token) {
        try {
            jwtParser.parseSignedClaims(token);
            return true;
        } catch (SecurityException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
            throw new TokenValidationException("Invalid JWT signature");
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            throw new TokenValidationException("Invalid JWT token");
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
            throw new TokenValidationException("JWT token is expired");
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
            throw new TokenValidationException("JWT token is unsupported");
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
            throw new TokenValidationException("JWT claims string is empty");
        } catch (io.jsonwebtoken.JwtException e) {
            logger.error("JWT validation error: {}", e.getMessage());
            throw new TokenValidationException("JWT validation error");
        }
    }

    public boolean validateRemotely(String token) {
        RestClient restClient = RestClient.create(remoteValidationUrl);
        TokenValidationResponse tokenValidationResponse = restClient
                .post()
                .body(Map.of("token", token))
                .retrieve()
                .body(TokenValidationResponse.class);
        return tokenValidationResponse != null && tokenValidationResponse.valid();
    }
}
