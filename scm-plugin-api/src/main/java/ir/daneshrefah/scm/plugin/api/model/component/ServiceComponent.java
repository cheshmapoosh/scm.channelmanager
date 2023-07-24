package ir.daneshrefah.scm.plugin.api.model.component;

import ir.daneshrefah.scm.common.model.BaseModel;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class ServiceComponent extends BaseModel<String> {

    private String code;
    private String title;
//    private String serviceComponentProviderId;
    private ServiceComponentProvider serviceComponentProvider;
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

//    public String getServiceComponentProviderId() {
//        return serviceComponentProviderId;
//    }
//
//    public void setServiceComponentProviderId(String serviceComponentProviderId) {
//        this.serviceComponentProviderId = serviceComponentProviderId;
//    }

    public ServiceComponentProvider getServiceComponentProvider() {
        return serviceComponentProvider;
    }

    public void setServiceComponentProvider(ServiceComponentProvider serviceComponentProvider) {
        this.serviceComponentProvider = serviceComponentProvider;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
