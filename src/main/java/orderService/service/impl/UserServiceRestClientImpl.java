package orderService.service.impl;

import orderService.dto.UserDto;
import orderService.exception.ServiceUnavailableException;
import orderService.exception.UserNotFoundException;
import orderService.service.UserServiceClient;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class UserServiceRestClientImpl implements UserServiceClient {

    private final RestClient userServiceRestClient;

    public UserServiceRestClientImpl(RestClient userServiceRestClient) {
        this.userServiceRestClient = userServiceRestClient;
    }

    @Override
    public UserDto findUserById(Long userId, JwtAuthenticationToken jwtAuthenticationToken) {
        Long userIdFromToken = extractUserIdFromJwtToken(jwtAuthenticationToken);
        if(!userIdFromToken.equals(userId)){
            throw new AccessDeniedException("User can only access their own information");
        }
        String USER_BY_ID_RELATIVE_API_URI = "api/v1/users/{userId}";
        return userServiceRestClient
                .post()
                .uri(USER_BY_ID_RELATIVE_API_URI,userId)
                .header("Authorization","Bearer "+jwtAuthenticationToken.getToken().getTokenValue())
                .retrieve()
                .onStatus((httpStatusCode -> httpStatusCode.isSameCodeAs(HttpStatusCode.valueOf(403))), (request, response) -> {
                    throw new AccessDeniedException("User does not have permission to access this resource");
                })
                .onStatus((httpStatusCode -> httpStatusCode.isSameCodeAs(HttpStatusCode.valueOf(404))), (request, response) -> {
                    throw new UserNotFoundException(userId);
                })
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new BadRequestException("Invalid request: " + response.getStatusText());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    throw new ServiceUnavailableException("User service is temporarily unavailable. Please try again later.");
                })
                .body(UserDto.class);

    }

    private Long extractUserIdFromJwtToken(JwtAuthenticationToken jwtAuthenticationToken){
        String userIdStringRepresentation = jwtAuthenticationToken.getToken().getClaimAsString("userId");
        if(userIdStringRepresentation == null){
            throw new RuntimeException();
        }
        return Long.valueOf(userIdStringRepresentation);
    }
}
