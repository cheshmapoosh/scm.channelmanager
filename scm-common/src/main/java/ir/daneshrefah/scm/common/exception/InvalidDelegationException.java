package ir.daneshrefah.scm.common.exception;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-19
 */
public class InvalidDelegationException extends BaseException {

    private final String username;

    public InvalidDelegationException(String username) {
        this(username, null);
    }

    public InvalidDelegationException(String username, Throwable cause) {
        super("user: " + username + ", hasn't delegation authority.", cause);
        this.username= username;
    }

    @Override
    public String getSource() {
        return username;
    }
}
