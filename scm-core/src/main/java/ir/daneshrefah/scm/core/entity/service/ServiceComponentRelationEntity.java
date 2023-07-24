package ir.daneshrefah.scm.core.entity.service;

import ir.daneshrefah.scm.core.converter.ServiceComponentRelationTypeConverter;
import ir.daneshrefah.scm.core.entity.AbstractEntity;
import ir.daneshrefah.scm.core.entity.component.ServiceComponentEntity;
import ir.daneshrefah.scm.plugin.api.model.service.ServiceComponentRelationType;
import jakarta.persistence.*;

@Entity
@Table(name = "TBL_SCM_SERVICE_COMPONENT_RELATION")
public class ServiceComponentRelationEntity extends AbstractEntity<String> {
    @Id
    @Column(name = "SERVICE_COMPONENT_RELATION_ID")
    private String id;
    @ManyToOne
    @JoinColumn(name = "SERVICE_COMPONENT_ID")
    private ServiceComponentEntity serviceComponentEntity;
    @ManyToOne
    @JoinColumn(name = "SERVICE_ID")
    private ServiceEntity serviceEntity;
    @Column(name = "REQUEST_TRANSFORMER_TYPE_CODE")
    @Convert(converter = ServiceComponentRelationTypeConverter.class)
    private ServiceComponentRelationType requestTransformerType;
    @Column(name = "RESPONSE_TRANSFORMER_TYPE_CODE")
    @Convert(converter = ServiceComponentRelationTypeConverter.class)
    private ServiceComponentRelationType responseTransformerType;
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

    public ServiceComponentEntity getServiceComponentEntity() {
        return serviceComponentEntity;
    }

    public void setServiceComponentEntity(ServiceComponentEntity serviceComponentEntity) {
        this.serviceComponentEntity = serviceComponentEntity;
    }

    public ServiceEntity getServiceEntity() {
        return serviceEntity;
    }

    public void setServiceEntity(ServiceEntity serviceEntity) {
        this.serviceEntity = serviceEntity;
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
