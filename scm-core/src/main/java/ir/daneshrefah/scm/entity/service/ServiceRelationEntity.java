package ir.daneshrefah.scm.entity.service;

import ir.daneshrefah.scm.common.model.service.ServiceRelation;
import ir.daneshrefah.scm.entity.AbstractEntity;
import ir.daneshrefah.scm.entity.component.ServiceComponentEntity;
import ir.daneshrefah.scm.repository.converter.ServiceRelationTypeConverter;
import jakarta.persistence.*;

@Entity
@Table(name = "TBL_SCM_SERVICE_RELATION")
public class ServiceRelationEntity extends AbstractEntity {
    @Id
    @Column(name = "SERVICE_RELATION_ID")
    private String id;
    @ManyToOne
    @JoinColumn(name = "service_component_id")
    private ServiceComponentEntity serviceComponentEntity;
    @ManyToOne
    @JoinColumn(name = "service_id")
    private ServiceEntity serviceEntity;
    @Column(name = "REQUEST_TRANSFORMER_TYPE_CODE")
    @Convert(converter = ServiceRelationTypeConverter.class)
    private ServiceRelation.ServiceRelationType requestTransformerType;
    @Column(name = "RESPONSE_TRANSFORMER_TYPE_CODE")
    @Convert(converter = ServiceRelationTypeConverter.class)
    private ServiceRelation.ServiceRelationType responseTransformerType;
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

    public ServiceRelation.ServiceRelationType getRequestTransformerType() {
        return requestTransformerType;
    }

    public void setRequestTransformerType(ServiceRelation.ServiceRelationType requestTransformerType) {
        this.requestTransformerType = requestTransformerType;
    }

    public ServiceRelation.ServiceRelationType getResponseTransformerType() {
        return responseTransformerType;
    }

    public void setResponseTransformerType(ServiceRelation.ServiceRelationType responseTransformerType) {
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
