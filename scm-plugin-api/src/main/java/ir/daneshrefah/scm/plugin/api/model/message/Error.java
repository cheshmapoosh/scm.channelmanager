package ir.daneshrefah.scm.plugin.api.model.message;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-23
 */
public class Error {
    private String code;
    private String message;
    private String source;
    private String sourceErrorCode;

    public Error(String code, String message, String source, String sourceErrorCode) {
        this.code = code;
        this.message = message;
        this.source = source;
        this.sourceErrorCode = sourceErrorCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceErrorCode() {
        return sourceErrorCode;
    }

    public void setSourceErrorCode(String sourceErrorCode) {
        this.sourceErrorCode = sourceErrorCode;
    }
}
