package ir.daneshrefah.scm.core.config;

import ir.daneshrefah.scm.common.model.Profile;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-22
 */
public class ApplicationConfig {

    private String activeProfileCode;
    private Profile activeProfile;

    public ApplicationConfig(Profile activeProfile) {
        this.activeProfile = activeProfile;
        this.activeProfileCode = activeProfile.getCode();
    }
}
