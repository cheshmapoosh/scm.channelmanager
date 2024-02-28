package ir.daneshrefah.scm.common.exception;

//    TODO ELI use the 'NoMatchRecordFoundException' class insteadof this class
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
