package ir.daneshrefah.scm.common.model.message;

import java.io.Serializable;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-11-20
 */
public interface IAuthenticationHeader extends Serializable {

    public String getName();

    public String getTerminalCode();

    public boolean isAnonymous();

    public boolean isAuthenticated();

    public boolean hasAuthority(String authorityName);

    public boolean hasError();

//    public Exception getException();

}
