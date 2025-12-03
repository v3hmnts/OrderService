package orderService.config;

import orderService.security.RequestContext;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;


public class MockRequestContext extends RequestContext {

    private final JwtAuthenticationToken jwtToken;

    public MockRequestContext() {
        Jwt jwt = Jwt.withTokenValue("mock-jwt-token")
                .header("alg", "RS256")
                .claim("sub", "test-user")
                .claim("userId", "12345") // This is what your RequestContext reads
                .claim("scope", "read write")
                .claim("roles", new String[]{"USER"})
                .build();

        this.jwtToken = new JwtAuthenticationToken(
                jwt,
                List.of(new SimpleGrantedAuthority("USER"), new SimpleGrantedAuthority("ADMIN"))
        );
    }

    @Override
    public JwtAuthenticationToken getJwtToken() {
        return this.jwtToken;
    }

    @Override
    public String getTokenValue() {
        return this.jwtToken.getToken().getTokenValue();
    }

    @Override
    public Long getUserId() {
        return 1L;
    }
}
