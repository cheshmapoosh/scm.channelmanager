package ir.daneshrefah.scm.config.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "TBL_SFG_PROPERTY_HISTORY")
public class PropertyHistoryEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ACTION")
    private String action;

    @Column(name = "PROPERTY_ID")
    private String propertyId;

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


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
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
}
