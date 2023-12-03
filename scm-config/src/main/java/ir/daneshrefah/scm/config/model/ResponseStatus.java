package ir.daneshrefah.scm.config.model;

import org.springframework.http.HttpStatus;

public enum ResponseStatus {

    SUCCESS(HttpStatus.OK.value(), "Success"),

    ;

    private final int code;
    private final String message;

    ResponseStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
