package ir.daneshrefah.scm.common.exception;

public class ScmException extends RuntimeException {
    private String code;

    public ScmException() {
        super();
    }

    public ScmException(String code, String message) {
        super(message);
        this.code = code;
    }

    public ScmException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

}
