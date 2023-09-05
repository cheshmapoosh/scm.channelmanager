package ir.daneshrefah.scm.core.entity.service.composition;

import ir.daneshrefah.scm.common.model.transformer.TransformerRelation;
import ir.daneshrefah.scm.core.converter.ServiceRelationTypeConverter;
import ir.daneshrefah.scm.core.entity.AbstractEntity;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.core.entity.transformer.TransformerRelationEntity;
import ir.daneshrefah.scm.plugin.api.model.service.composition.ServiceRelationType;
import jakarta.persistence.*;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-08
 */
@Entity
@Table(name = "TBL_SCM_SERVICE_RELATION")
public class ServiceRelationEntity extends AbstractEntity<String> {

    @Id
    @Column(name = "SERVICE_RELATION_ID")
    private String id;
    @ManyToOne
    @JoinColumn(name = "SOURCE_SERVICE_ID")
    private ServiceEntity sourceService;
    private Integer order;
    @Column(name = "RELATION_TYPE_CODE")
    @Convert(converter = ServiceRelationTypeConverter.class)
    private ServiceRelationType relationType;
    @ManyToOne
    @JoinColumn(name = "TARGET_SERVICE_ID")
    private ServiceEntity targetService;
    @ManyToOne
    @JoinColumn(name = "TARGET_SERVICE_COMMIT_ID")
    private ServiceEntity targetServiceCommit;
    @ManyToOne
    @JoinColumn(name = "TARGET_SERVICE_REVERSE_ID")
    private ServiceEntity targetServiceReverse;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public ServiceEntity getSourceService() {
        return sourceService;
    }

    public void setSourceService(ServiceEntity sourceService) {
        this.sourceService = sourceService;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public ServiceRelationType getRelationType() {
        return relationType;
    }

    public void setRelationType(ServiceRelationType relationType) {
        this.relationType = relationType;
    }

    public ServiceEntity getTargetService() {
        return targetService;
    }

    public void setTargetService(ServiceEntity targetService) {
        this.targetService = targetService;
    }

    public ServiceEntity getTargetServiceCommit() {
        return targetServiceCommit;
    }

    public void setTargetServiceCommit(ServiceEntity targetServiceCommit) {
        this.targetServiceCommit = targetServiceCommit;
    }

    public ServiceEntity getTargetServiceReverse() {
        return targetServiceReverse;
    }

    public void setTargetServiceReverse(ServiceEntity targetServiceReverse) {
        this.targetServiceReverse = targetServiceReverse;
    }
}
