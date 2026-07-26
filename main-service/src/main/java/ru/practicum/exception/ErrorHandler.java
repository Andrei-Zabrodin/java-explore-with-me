package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handle(final NotFoundException e) {
        log.warn("404 Not Found: {}", e.getMessage());
        return new ApiError(
                HttpStatus.NOT_FOUND,
                "The required object was not found.",
                e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handle(final MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("400 Bad Request: {}", message);
        return new ApiError(
                HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                message
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handle(final MethodArgumentTypeMismatchException e) {
        log.warn("400 Bad Request: {}", e.getMessage());
        return new ApiError(
                HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handle(ValidationException e) {
        log.warn("400 Bad Request: {}", e.getMessage());
        return new ApiError(
                HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handle(ConflictException e) {
        log.warn("409 Conflict: {}", e.getMessage());
        return new ApiError(
                HttpStatus.CONFLICT,
                "For the requested operation the conditions are not met.",
                e.getMessage()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handle(ConstraintViolationException e) {
        log.warn("409 Conflict: {}", e.getMessage());
        return new ApiError(
                HttpStatus.CONFLICT,
                "Integrity constraint has been violated.",
                e.getMessage()
        );
    }
}
