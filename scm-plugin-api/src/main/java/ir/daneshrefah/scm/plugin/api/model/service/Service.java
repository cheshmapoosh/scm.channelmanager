package ir.daneshrefah.scm.plugin.api.model.service;

import ir.daneshrefah.scm.common.model.BaseModel;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class Service extends BaseModel<String> {

    private String code;
    private String title;
    private ServiceImplementationType implementationType;
    private String requestJSONSchema;
    private String responseJSONSchema;
    private ServiceImplementation implementation;
    private Service parent;

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

    public ServiceImplementationType getImplementationType() {
        return implementationType;
    }

    public void setImplementationType(ServiceImplementationType implementationType) {
        this.implementationType = implementationType;
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

    public ServiceImplementation getImplementation() {
        return implementation;
    }

    public void setImplementation(ServiceImplementation implementation) {
        this.implementation = implementation;
    }

    public Service getParent() {
        return parent;
    }

    public void setParent(Service parent) {
        this.parent = parent;
    }

}
