package ir.daneshrefah.scm.common.model.terminal;

import ir.daneshrefah.scm.common.BaseModel;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class Terminal extends BaseModel<String> {

    private String code;
    private String title;
    private Boolean supportCheckAuthentication;
    private Boolean supportCheckSecondAuthentication;
    private Boolean supportCheckServiceAccess;
    private Boolean supportCheckAssetAccess;

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
