package ir.daneshrefah.scm.common.model.component;

import ir.daneshrefah.scm.common.model.BaseModel;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class ServiceComponent extends BaseModel {

    private String code;
    private String title;
    private ServiceComponentProvider provider;
    private String requestJSONSchema;
    private String responseJSONSchema;
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

    public ServiceComponentProvider getProvider() {
        return provider;
    }

    public void setProvider(ServiceComponentProvider provider) {
        this.provider = provider;
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
}
