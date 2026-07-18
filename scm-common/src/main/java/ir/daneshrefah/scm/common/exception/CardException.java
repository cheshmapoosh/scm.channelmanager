package ir.daneshrefah.scm.common.exception;

import lombok.Getter;

@Getter
public class CardException extends RuntimeException {
    private String code;
    private String isoErrorCode;

    public CardException() {
        super();
    }

    public CardException(String code, String message) {
        super(message);
        this.code = code;
    }
    public CardException(String code, String isoErrorCode, String message) {
        super(message);
        this.code = code;
        this.isoErrorCode = isoErrorCode;
    }

    public CardException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
