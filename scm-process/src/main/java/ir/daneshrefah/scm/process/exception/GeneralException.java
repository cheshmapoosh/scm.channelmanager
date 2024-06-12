package ir.daneshrefah.scm.process.exception;

public class GeneralException extends AbstractProcessException{

    public GeneralException(String source, String message) {
        super(source, message);
    }

    @Override
    public int getErrorCode() {
        return 0;
    }
}
