package ir.daneshrefah.scm.process.exception;

public class JsonNodeNullException extends AbstractProcessException{

    public JsonNodeNullException(String source, String message) {
        super(source, message);
    }

    @Override
    public int getErrorCode() {
        return 1;
    }//TODO change this error code
}
