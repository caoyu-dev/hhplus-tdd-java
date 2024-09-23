package io.hhplus.tdd;

import lombok.Getter;

@Getter
public enum ErrorCode {
    ADD_UNDER_VALUE_FAILED(400, "포인트는 0보다 큰 값이어야 합니다."),
    ;

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
