package ir.daneshrefah.scm.common.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "TBL_SCM_TERMINAL")
public class TerminalEntity extends AbstractDefaultEntity<String> {
    @Id
    @Column(name = "TERMINAL_ID")
    private String id;
    private String code;
    private String title;
    private Boolean supportCheckAuthentication;
    private Boolean supportCheckSecondAuthentication;
    private Boolean supportCheckServiceAccess;
    private Boolean supportCheckAssetAccess;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getSupportCheckAuthentication() {
        return supportCheckAuthentication;
    }

    public void setSupportCheckAuthentication(Boolean supportCheckAuthentication) {
        this.supportCheckAuthentication = supportCheckAuthentication;
    }

    public Boolean getSupportCheckSecondAuthentication() {
        return supportCheckSecondAuthentication;
    }

    public void setSupportCheckSecondAuthentication(Boolean supportCheckSecondAuthentication) {
        this.supportCheckSecondAuthentication = supportCheckSecondAuthentication;
    }

    public Boolean getSupportCheckServiceAccess() {
        return supportCheckServiceAccess;
    }

    public void setSupportCheckServiceAccess(Boolean supportCheckServiceAccess) {
        this.supportCheckServiceAccess = supportCheckServiceAccess;
    }

    public Boolean getSupportCheckAssetAccess() {
        return supportCheckAssetAccess;
    }

    public void setSupportCheckAssetAccess(Boolean supportCheckAssetAccess) {
        this.supportCheckAssetAccess = supportCheckAssetAccess;
    }

}
