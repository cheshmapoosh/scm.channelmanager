package ir.daneshrefah.scm.plugin.api.model.service;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.plugin.api.model.BaseModel;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
public abstract class Service extends BaseModel<String> {

    private String code;
    private String title;
    private String metadata;
    private ServiceImplementationType implementationType;
    private String requestJsonSchema;
    private String responseJsonSchema;
    private TransformerType requestTransformerType;
    private TransformerType responseTransformerType;
    private String requestTransformMetadata;
    private String responseTransformMetadata;
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

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public ServiceImplementationType getImplementationType() {
        return implementationType;
    }

    public void setImplementationType(ServiceImplementationType implementationType) {
        this.implementationType = implementationType;
    }

    public String getRequestJsonSchema() {
        return requestJsonSchema;
    }

    public void setRequestJsonSchema(String requestJsonSchema) {
        this.requestJsonSchema = requestJsonSchema;
    }

    public String getResponseJsonSchema() {
        return responseJsonSchema;
    }

    public void setResponseJsonSchema(String responseJsonSchema) {
        this.responseJsonSchema = responseJsonSchema;
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

    public String getRequestTransformMetadata() {
        return requestTransformMetadata;
    }

    public void setRequestTransformMetadata(String requestTransformMetadata) {
        this.requestTransformMetadata = requestTransformMetadata;
    }

    public String getResponseTransformMetadata() {
        return responseTransformMetadata;
    }

    public void setResponseTransformMetadata(String responseTransformMetadata) {
        this.responseTransformMetadata = responseTransformMetadata;
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

    public abstract Object getServiceInfo();

}
