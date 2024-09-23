package io.hhplus.tdd;

import lombok.Getter;

@Getter
public enum ErrorCode {
    ADD_UNDER_VALUE_FAILED(400, "포인트는 0보다 큰 값이어야 합니다."),
    USER_NOT_FOUND(400, "해당 유저는 존재하지 않습니다."),
    INVALID_OPERATION(400, "사용할 포인트는 0보다 커야 합니다."),
    INSUFFICIENT_BALANCE(400, "사용할 포인트가 충분하지 않습니다.")
    ;

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
