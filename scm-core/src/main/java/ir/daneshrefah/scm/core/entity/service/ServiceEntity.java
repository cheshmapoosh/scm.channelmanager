package ir.daneshrefah.scm.core.entity.service;

import ir.daneshrefah.scm.core.converter.ServiceImplementationTypeConverter;
import ir.daneshrefah.scm.core.converter.TransformerTypeConverter;
import ir.daneshrefah.scm.core.entity.AbstractEntity;
import ir.daneshrefah.scm.plugin.api.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.plugin.api.model.service.TransformerType;
import jakarta.persistence.*;

@Entity
@Table(name = "TBL_SCM_SERVICE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "SERVICE_IMPLEMENTATION_TYPE_CODE", discriminatorType = DiscriminatorType.INTEGER)
public abstract class ServiceEntity extends AbstractEntity<String> {

    @Id
    @Column(name = "SERVICE_ID")
    private String id;
    private String code;
    private String title;
    private String metadata;
    @Column(name = "SERVICE_IMPLEMENTATION_TYPE_CODE", insertable = false, updatable = false)
    @Convert(converter = ServiceImplementationTypeConverter.class)
    private ServiceImplementationType implementationType;
    @Column(name = "REQUEST_JSON_SCHEMA", nullable = true)
    private String requestJsonSchema;
    @Column(name = "RESPONSE_JSON_SCHEMA", nullable = true)
    private String responseJsonSchema;
    @Column(name = "TRANSFORMER_TYPE_CODE_REQUEST")
    @Convert(converter = TransformerTypeConverter.class)
    private TransformerType requestTransformerType;
    @Column(name = "TRANSFORMER_TYPE_CODE_RESPONSE")
    @Convert(converter = TransformerTypeConverter.class)
    private TransformerType responseTransformerType;
    @Column(name = "TRANSFORM_METADATA_REQUEST")
    private String requestTransformMetadata;
    @Column(name = "TRANSFORM_METADATA_RESPONSE")
    private String responseTransformMetadata;
    @Column(name = "TRANSFORM_CLASS_REQUEST")
    private String requestTransformerClass;
    @Column(name = "TRANSFORM_CLASS_RESPONSE")
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

    public void setRequestTransformMetadata(String requestMetadata) {
        this.requestTransformMetadata = requestMetadata;
    }

    public String getResponseTransformMetadata() {
        return responseTransformMetadata;
    }

    public void setResponseTransformMetadata(String responseMetadata) {
        this.responseTransformMetadata = responseMetadata;
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
