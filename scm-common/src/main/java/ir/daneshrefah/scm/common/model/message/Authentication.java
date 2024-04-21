package ir.daneshrefah.scm.common.model.message;

import ir.daneshrefah.scm.common.model.customer.UserProfile;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;

import java.io.Serializable;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-11-20
 */
public interface Authentication extends Serializable {

    String getName();

    UserProfile getProfile();

    String getTerminalCode();

    boolean isAnonymous();

    boolean isDelegated();

    boolean isAuthenticated();

    boolean isFullyAuthenticated();

    boolean hasAuthority(String authorityName);

    boolean hasError();

    AuthenticationMethod getAuthenticationMethod();
//    Exception getException();

}
