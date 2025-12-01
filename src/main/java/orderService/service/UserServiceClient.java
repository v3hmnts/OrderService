package orderService.service;

import orderService.dto.UserDto;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public interface UserServiceClient {

    public UserDto findUserById(Long userId, JwtAuthenticationToken jwtAuthenticationToken);

}
