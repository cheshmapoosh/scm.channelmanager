package ir.daneshrefah.scm.common.model.service;

import ir.daneshrefah.scm.common.model.BaseModel;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class Service extends BaseModel {

    private String code;
    private String title;
    private ServiceType type;
    private String requestJSONSchema;
    private String responseJSONSchema;
    private ServiceImplementation implementation;

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

    public ServiceType getType() {
        return type;
    }

    public void setType(ServiceType type) {
        this.type = type;
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
}
