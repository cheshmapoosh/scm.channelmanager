package ir.daneshrefah.scm.config;

import java.io.Serializable;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-11-18
 */
public class PropertyId implements Serializable {
    private String applicationKey;

    private String profileKey;

    private String labelKey;
    private String propKey;

    public PropertyId(String applicationKey, String profileKey, String labelKey, String propKey) {
        this.applicationKey = applicationKey;
        this.profileKey = profileKey;
        this.labelKey = labelKey;
        this.propKey = propKey;
    }

    public String getApplicationKey() {
        return applicationKey;
    }

    public void setApplicationKey(String applicationKey) {
        this.applicationKey = applicationKey;
    }

    public String getProfileKey() {
        return profileKey;
    }

    public void setProfileKey(String profileKey) {
        this.profileKey = profileKey;
    }

    public String getLabelKey() {
        return labelKey;
    }

    public void setLabelKey(String labelKey) {
        this.labelKey = labelKey;
    }

    public String getPropKey() {
        return propKey;
    }

    public void setPropKey(String propKey) {
        this.propKey = propKey;
    }
}
