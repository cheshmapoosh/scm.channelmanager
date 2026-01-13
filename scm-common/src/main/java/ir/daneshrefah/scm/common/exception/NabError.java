package ir.daneshrefah.scm.common.exception;

import lombok.Getter;

@Getter
public class NabError extends RuntimeException {
    private String code;

    public NabError() {
        super();
    }

    public NabError(String code, String message) {
        super(message);
        this.code = code;
    }

    public NabError(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
