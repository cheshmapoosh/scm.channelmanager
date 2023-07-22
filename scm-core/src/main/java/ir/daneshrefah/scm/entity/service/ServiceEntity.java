package ir.daneshrefah.scm.entity.service;

import ir.daneshrefah.scm.common.model.service.ServiceImplementation;
import ir.daneshrefah.scm.common.model.service.ServiceType;
import ir.daneshrefah.scm.entity.AbstractEntity;
import ir.daneshrefah.scm.repository.converter.ServiceTypeConverter;
import jakarta.persistence.*;

@Entity
@Table(name = "TBL_SCM_SERVICE")
public class ServiceEntity extends AbstractEntity {
    @Id
    @Column(name = "SERVICE_ID")
    private String id;
    private String code;
    private String title;
    @Column(name = "SERVICE_TYPE_CODE")
    @Convert(converter = ServiceTypeConverter.class)
    private ServiceType type;
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
