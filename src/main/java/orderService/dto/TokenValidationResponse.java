package orderService.dto;

public record TokenValidationResponse(boolean valid, String username, String message) {
}
