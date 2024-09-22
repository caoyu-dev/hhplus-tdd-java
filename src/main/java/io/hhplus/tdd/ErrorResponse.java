package io.hhplus.tdd;

public record ErrorResponse(
        String code,
        String message
) {
    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
