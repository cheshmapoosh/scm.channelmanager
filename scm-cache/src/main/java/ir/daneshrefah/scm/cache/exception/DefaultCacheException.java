package ir.daneshrefah.scm.cache.exception;

import lombok.Getter;

@Getter
public class DefaultCacheException extends RuntimeException {
    private final String message;
    private final String code;

    public DefaultCacheException(String message, String code) {
        super(message);
        this.message = message;
        this.code = code;
    }
}
