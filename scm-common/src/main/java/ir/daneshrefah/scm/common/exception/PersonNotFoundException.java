package ir.daneshrefah.scm.common.exception;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-13
 */
public class PersonNotFoundException extends BasePersonException {


    public PersonNotFoundException(String message) {
        super(message);
    }

    @Override
    public String getSource() {
        return null;
    }

}
