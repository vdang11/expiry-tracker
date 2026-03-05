package com.example.expiry.domain;

public class InvalidImageException extends RuntimeException {

    private final String code;

    public InvalidImageException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}