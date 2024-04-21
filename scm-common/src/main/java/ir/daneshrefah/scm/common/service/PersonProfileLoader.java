package ir.daneshrefah.scm.common.service;

import ir.daneshrefah.scm.common.model.customer.UserProfile;
import ir.daneshrefah.scm.common.model.message.Authentication;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-21
 */
public interface PersonProfileLoader {

    UserProfile preparePersonProfile(Authentication authentication);

    UserProfile preparePersonProfileMemberships(UserProfile profile, String terminalCode);

    UserProfile preparePersonProfileMemberships(Authentication authentication);

    UserProfile fillServiceAccessForProfile(UserProfile profile, String terminalCode);

}
