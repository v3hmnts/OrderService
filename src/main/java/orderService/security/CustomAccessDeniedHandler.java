package orderService.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import orderService.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                "Access denied: " + accessDeniedException.getMessage(),
                HttpStatus.FORBIDDEN,
                null
        );

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
