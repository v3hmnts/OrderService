package orderService.service.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import orderService.dto.UserDto;
import orderService.exception.ServiceUnavailableException;
import orderService.exception.UserNotFoundException;
import orderService.security.RequestContext;
import orderService.service.UserServiceClient;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class UserServiceRestClientImpl implements UserServiceClient {

    private final RestClient userServiceRestClient;
    private final RequestContext requestContext;
    private static final Logger logger = LoggerFactory.getLogger(UserServiceRestClientImpl.class);

    public UserServiceRestClientImpl(RestClient userServiceRestClient, RequestContext requestContext) {
        this.userServiceRestClient = userServiceRestClient;
        this.requestContext = requestContext;
    }

    @Override
    @CircuitBreaker(name = "user-service")
    public UserDto findUserById(Long userId) {

        String tokenValue = requestContext.getTokenValue();
        if (tokenValue == null) {
            throw new AccessDeniedException("No authentication token available");
        }
        String USER_BY_ID_RELATIVE_API_URI = "api/v1/users/{userId}";
        return userServiceRestClient
                .get()
                .uri(USER_BY_ID_RELATIVE_API_URI, userId)
                .header("Authorization", "Bearer " + tokenValue)
                .retrieve()
                .onStatus((httpStatusCode -> httpStatusCode.isSameCodeAs(HttpStatusCode.valueOf(403))), (request, response) -> {
                    logger.warn("Access denied for user {} to resource {}", requestContext.getUserId(), userId);
                    throw new AccessDeniedException("User does not have permission to access this resource");
                })
                .onStatus((httpStatusCode -> httpStatusCode.isSameCodeAs(HttpStatusCode.valueOf(404))), (request, response) -> {
                    logger.warn("User with id {} not found", userId);
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

}
