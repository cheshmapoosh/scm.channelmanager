package ir.daneshrefah.scm.common.exception;

//    TODO ELI use the 'InputAlreadyExistException' class insteadof this class
public class RoleAlreadyExistsException extends RuntimeException {

    public RoleAlreadyExistsException(String message) {
        super(message);
    }
}
