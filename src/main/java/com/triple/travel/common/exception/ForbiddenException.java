package com.triple.travel.common.exception;

public class ForbiddenException extends RuntimeException {

    private final String errorCode;

    public ForbiddenException(String errorCode) {
        super(errorCode);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
