package orderService.security;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestContext {

    private JwtAuthenticationToken jwtToken;

    public JwtAuthenticationToken getJwtToken() {
        if (jwtToken == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken) {
                jwtToken = (JwtAuthenticationToken) auth;
            }
        }
        return jwtToken;
    }

    public String getTokenValue() {
        JwtAuthenticationToken token = getJwtToken();
        return token != null ? token.getToken().getTokenValue() : null;
    }

    public Long getUserId() {
        JwtAuthenticationToken token = getJwtToken();
        if (token == null) return null;

        String userId = token.getToken().getClaimAsString("userId");
        return userId != null ? Long.valueOf(userId) : null;
    }
}