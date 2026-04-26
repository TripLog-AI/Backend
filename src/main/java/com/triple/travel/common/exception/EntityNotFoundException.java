package com.triple.travel.common.exception;

public class EntityNotFoundException extends RuntimeException {

    private final String errorCode;

    public EntityNotFoundException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

    public EntityNotFoundException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
