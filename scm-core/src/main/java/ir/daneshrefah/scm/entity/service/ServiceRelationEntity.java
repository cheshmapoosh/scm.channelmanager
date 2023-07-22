package ir.daneshrefah.scm.entity.service;

import ir.daneshrefah.scm.entity.AbstractEntity;
import ir.daneshrefah.scm.entity.component.ServiceComponentEntity;
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
}
