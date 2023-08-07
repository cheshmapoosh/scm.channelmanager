package ir.daneshrefah.scm.plugin.api.model.service.external;

import ir.daneshrefah.scm.plugin.api.model.BaseModel;

public class ExternalServiceProvider extends BaseModel<String> {
    private String code;
    private String title;
    private String providerClassName;
    private String metadata;

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

    public String getProviderClassName() {
        return providerClassName;
    }

    public void setProviderClassName(String componentClassName) {
        this.providerClassName = componentClassName;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

}
