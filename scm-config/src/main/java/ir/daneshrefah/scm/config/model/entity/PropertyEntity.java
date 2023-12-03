package ir.daneshrefah.scm.config.model.entity;

import jakarta.persistence.*;


import java.io.Serializable;

@Entity
@Table(name = "TBL_SFG_PROPERTY")
@EntityListeners(PropertyEntityListener.class)
public class PropertyEntity implements Serializable{

    @Id
    @Column(name = "PROPERTY_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(name = "APPLICATION_ID")
    private String applicationId;

    @Column(name = "PROFILE_ID")
    private String profileId;

    @Column(name = "LABEL_KEY")
    private String labelKey;

    @Column(name = "PROP_VALUE")
    private String propValue;

    @Column(name = "PROP_KEY")
    private String propKey;

    @Column(name = "TITLE")
    private String title;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getLabelKey() {
        return labelKey;
    }

    public void setLabelKey(String labelKey) {
        this.labelKey = labelKey;
    }

    public String getPropValue() {
        return propValue;
    }

    public void setPropValue(String propValue) {
        this.propValue = propValue;
    }

    public String getPropKey() {
        return propKey;
    }

    public void setPropKey(String propKey) {
        this.propKey = propKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
