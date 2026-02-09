package orderService.util;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;

public class TestJwtAuthenticationTokenSupplier {

    public static JwtAuthenticationToken getJwtAuthenticationTokenWithUserRole() {
        Jwt jwt = Jwt.withTokenValue("userToken")
                .header("alg", "none")
                .claim("userId", "1")
                .build();

        return new JwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    public static JwtAuthenticationToken getJwtAuthenticationTokenWithAdminRole() {
        Jwt jwt = Jwt.withTokenValue("adminToken")
                .header("alg", "none")
                .claim("userId", "1")
                .build();

        return new JwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

}
