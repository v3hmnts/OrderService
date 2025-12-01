package orderService.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
public class CustomJwtDecoder implements JwtDecoder {

    private static final Logger logger = LoggerFactory.getLogger(CustomJwtDecoder.class);
    private final CustomJwtValidator jwtValidator;

    public CustomJwtDecoder(CustomJwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        if (!jwtValidator.validateToken(token)) {
            throw new RuntimeException("Jwt token is not valid");
        }
        Claims claims = getClaims(token);
        Header header = getHeader(token);
        return new Jwt(token, claims.getIssuedAt().toInstant(), claims.getExpiration().toInstant(), header, claims);
    }

    private Claims getClaims(String token) {
        return jwtValidator.getJwtParser()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Header getHeader(String token) {
        return jwtValidator.getJwtParser()
                .parseSignedClaims(token)
                .getHeader();
    }

}
