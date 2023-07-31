package ir.daneshrefah.scm.core.entity.component;

import ir.daneshrefah.scm.core.converter.TransformerTypeConverter;
import ir.daneshrefah.scm.core.entity.AbstractEntity;
import ir.daneshrefah.scm.plugin.api.model.service.TransformerType;
import jakarta.persistence.*;

@Entity
@Table(name = "TBL_SCM_SERVICE_COMPONENT")
public class ServiceComponentEntity extends AbstractEntity<String> {
    @Id
    @Column(name = "SERVICE_COMPONENT_ID")
    private String id;
    private String code;
    private String title;
//    private String serviceComponentProviderId;
    @ManyToOne
    @JoinColumn(name = "SERVICE_COMPONENT_PROVIDER_ID")
    private ServiceComponentProviderEntity serviceComponentProviderEntity;
    @Column(name = "REQUEST_JSON_SCHEMA", nullable = true)
    private String requestJSONSchema;
    @Column(name = "RESPONSE_JSON_SCHEMA", nullable = true)
    private String responseJSONSchema;
    private String metadata;
    @Column(name = "REQUEST_TRANSFORMER_TYPE_CODE")
    @Convert(converter = TransformerTypeConverter.class)
    private TransformerType requestTransformerType;
    @Column(name = "RESPONSE_TRANSFORMER_TYPE_CODE")
    @Convert(converter = TransformerTypeConverter.class)
    private TransformerType responseTransformerType;
    private String requestMetadata;
    private String responseMetadata;
    private String requestTransformerClass;
    private String responseTransformerClass;

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

    /*public String getServiceComponentProviderId() {
        return serviceComponentProviderId;
    }

    public void setServiceComponentProviderId(String serviceComponentProviderId) {
        this.serviceComponentProviderId = serviceComponentProviderId;
    }*/

    public ServiceComponentProviderEntity getServiceComponentProviderEntity() {
        return serviceComponentProviderEntity;
    }

    public void setServiceComponentProviderEntity(ServiceComponentProviderEntity serviceComponentProviderEntity) {
        this.serviceComponentProviderEntity = serviceComponentProviderEntity;
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
