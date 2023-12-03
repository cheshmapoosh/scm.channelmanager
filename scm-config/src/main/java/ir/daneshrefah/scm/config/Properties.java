package ir.daneshrefah.scm.config;

import java.io.Serializable;

public class Properties implements Serializable {

    private String applicationKey;
    private String profileKey;
    private String labelKey;
    private String propKey;
    private String propValue;

    public Properties(String applicationKey, String profileKey, String labelKey, String propKey, String propValue) {
        this.applicationKey = applicationKey;
        this.profileKey = profileKey;
        this.labelKey = labelKey;
        this.propKey = propKey;
        this.propValue = propValue;
    }

    public String getApplicationKey() {
        return applicationKey;
    }

    public String getProfileKey() {
        return profileKey;
    }

    public String getLabelKey() {
        return labelKey;
    }

    public String getPropKey() {
        return propKey;
    }

    public String getPropValue() {
        return propValue;
    }
}
