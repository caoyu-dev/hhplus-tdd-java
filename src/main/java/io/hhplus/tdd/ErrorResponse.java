package io.hhplus.tdd;

public record ErrorResponse(
        int status,
        String message
) {
    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
