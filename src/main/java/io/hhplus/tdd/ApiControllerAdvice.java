package io.hhplus.tdd;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
class ApiControllerAdvice extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = BaseException.class)
    public ResponseEntity<?> handleInvalidAmountException(BaseException e) {
        int status = 500;

        if (e instanceof BaseException) {
            BaseException be = (BaseException) e;
            status = be.getErrorCode().getStatus();
        }

        return getResponseEntity(status, e.getMessage(), HttpStatus.valueOf(status));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        return getResponseEntity(500, "에러가 발생했습니다.", HttpStatus.valueOf(500));
    }

    private ResponseEntity<ErrorResponse> getResponseEntity(int status, String message, HttpStatus httpStatus) {
        ErrorResponse error = new ErrorResponse(status, message);
        return new ResponseEntity<>(error, httpStatus);
    }
}
