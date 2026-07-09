package com.qst.crop.exception;

/**
 * 登录异常
 */
public class LoginException extends RuntimeException {

    private final String message;
    private final int statusCode;

    public LoginException(String message) {
        this(message, 401);
    }

    public LoginException(String message, int statusCode) {
        super(message);
        this.message = message;
        this.statusCode = statusCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
