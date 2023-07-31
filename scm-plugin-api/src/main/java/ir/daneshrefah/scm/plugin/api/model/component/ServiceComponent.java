package ir.daneshrefah.scm.plugin.api.model.component;

import ir.daneshrefah.scm.common.model.BaseModel;
import ir.daneshrefah.scm.plugin.api.model.service.TransformerType;

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
    private String requestJSONSchema;
    private String responseJSONSchema;
    private String metadata;
    private TransformerType requestTransformerType;
    private TransformerType responseTransformerType;
    private String requestMetadata;
    private String responseMetadata;
    private String requestTransformerClass;
    private String responseTransformerClass;

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

    public String getRequestJSONSchema() {
        return requestJSONSchema;
    }

    public void setRequestJSONSchema(String requestJSONSchema) {
        this.requestJSONSchema = requestJSONSchema;
    }

    public String getResponseJSONSchema() {
        return responseJSONSchema;
    }

    public void setResponseJSONSchema(String responseJSONSchema) {
        this.responseJSONSchema = responseJSONSchema;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public TransformerType getRequestTransformerType() {
        return requestTransformerType;
    }

    public void setRequestTransformerType(TransformerType requestTransformerType) {
        this.requestTransformerType = requestTransformerType;
    }

    public TransformerType getResponseTransformerType() {
        return responseTransformerType;
    }

    public void setResponseTransformerType(TransformerType responseTransformerType) {
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
