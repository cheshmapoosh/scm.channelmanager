package ir.daneshrefah.scm.core.entity.service;

import ir.daneshrefah.scm.core.converter.ServiceImplementationTypeConverter;
import ir.daneshrefah.scm.core.entity.AbstractEntity;
import ir.daneshrefah.scm.plugin.api.model.service.ServiceImplementationType;
import jakarta.persistence.*;

@Entity
@Table(name = "TBL_SCM_SERVICE")
public class ServiceEntity extends AbstractEntity<String> {
    @Id
    @Column(name = "SERVICE_ID")
    private String id;
    private String code;
    private String title;
    @Column(name = "SERVICE_IMPLEMENTATION_TYPE_CODE")
    @Convert(converter = ServiceImplementationTypeConverter.class)
    private ServiceImplementationType implementationType;
    @Column(name = "REQUEST_JSON_SCHEMA", nullable = true)
    private String requestJSONSchema;
    @Column(name = "RESPONSE_JSON_SCHEMA", nullable = true)
    private String responseJSONSchema;
    private String javaImplementationClassName;
    private String bpmnImplementationContent;

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

    public ServiceImplementationType getImplementationType() {
        return implementationType;
    }

    public void setImplementationType(ServiceImplementationType type) {
        this.implementationType = type;
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

    public String getJavaImplementationClassName() {
        return javaImplementationClassName;
    }

    public void setJavaImplementationClassName(String javaImplementationClassName) {
        this.javaImplementationClassName = javaImplementationClassName;
    }

    public String getBpmnImplementationContent() {
        return bpmnImplementationContent;
    }

    public void setBpmnImplementationContent(String bpmnImplementationContent) {
        this.bpmnImplementationContent = bpmnImplementationContent;
    }
}
