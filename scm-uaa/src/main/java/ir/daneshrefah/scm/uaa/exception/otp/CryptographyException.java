package ir.daneshrefah.scm.uaa.exception.otp;

public class CryptographyException extends RuntimeException {

    /**
     * Instantiates a new cryptography exception.
     *
     * @param msg the msg
     */
    public CryptographyException(String msg) {
        super(msg);
    }

    /**
     * Instantiates a new cryptography exception.
     *
     * @param msg the msg
     * @param cause the cause
     */
    public CryptographyException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
