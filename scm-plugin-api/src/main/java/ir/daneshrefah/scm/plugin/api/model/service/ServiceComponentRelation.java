package ir.daneshrefah.scm.plugin.api.model.service;


import ir.daneshrefah.scm.plugin.api.model.component.ServiceComponent;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class ServiceComponentRelation {

    private ServiceComponent serviceComponent;
    private ServiceComponentRelationType requestTransformerType;
    private ServiceComponentRelationType responseTransformerType;
    private String requestMetadata;
    private String responseMetadata;
    private String requestTransformerClass;
    private String responseTransformerClass;

    public ServiceComponent getServiceComponent() {
        return serviceComponent;
    }

    public void setServiceComponent(ServiceComponent serviceComponent) {
        this.serviceComponent = serviceComponent;
    }

    public ServiceComponentRelationType getRequestTransformerType() {
        return requestTransformerType;
    }

    public void setRequestTransformerType(ServiceComponentRelationType requestTransformerType) {
        this.requestTransformerType = requestTransformerType;
    }

    public ServiceComponentRelationType getResponseTransformerType() {
        return responseTransformerType;
    }

    public void setResponseTransformerType(ServiceComponentRelationType responseTransformerType) {
        this.responseTransformerType = responseTransformerType;
    }

    public String getRequestMetadata() {
        return requestMetadata;
    }

    public void setRequestMetadata(String requestMetadata) {
        this.requestMetadata = requestMetadata;
    }

    public String getResponseMetadata() {
        return responseMetadata;
    }

    public void setResponseMetadata(String responseMetadata) {
        this.responseMetadata = responseMetadata;
    }

    public String getRequestTransformerClass() {
        return requestTransformerClass;
    }

    public void setRequestTransformerClass(String requestTransformerClass) {
        this.requestTransformerClass = requestTransformerClass;
    }

    public String getResponseTransformerClass() {
        return responseTransformerClass;
    }

    public void setResponseTransformerClass(String responseTransformerClass) {
        this.responseTransformerClass = responseTransformerClass;
    }

}
