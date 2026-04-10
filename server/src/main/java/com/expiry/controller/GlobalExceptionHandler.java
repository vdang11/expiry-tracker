package com.expiry.controller;

import com.expiry.exception.InvalidImageException;
import com.expiry.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidImageException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidImage(InvalidImageException e) {
        ApiErrorResponse body = new ApiErrorResponse(e.getCode(), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleIllegalArgument(IllegalArgumentException ex) {
        return new ApiErrorResponse("BAD_REQUEST", ex.getMessage());
    }
}