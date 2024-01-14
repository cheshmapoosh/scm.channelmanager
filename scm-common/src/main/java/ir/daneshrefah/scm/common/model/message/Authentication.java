package ir.daneshrefah.scm.common.model.message;

import ir.daneshrefah.scm.common.model.person.PersonProfile;

import java.io.Serializable;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-11-20
 */
public interface Authentication extends Serializable {

    public String getName();

    public PersonProfile getPersonProfile();

    public String getTerminalCode();

    public boolean isAnonymous();

    public boolean isAuthenticated();

    public boolean hasAuthority(String authorityName);

    public boolean hasError();

//    public Exception getException();

}
