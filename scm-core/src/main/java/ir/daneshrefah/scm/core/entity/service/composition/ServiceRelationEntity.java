package ir.daneshrefah.scm.core.entity.service.composition;

import ir.daneshrefah.scm.core.converter.ServiceRelationTypeConverter;
import ir.daneshrefah.scm.core.converter.TransformerTypeConverter;
import ir.daneshrefah.scm.core.entity.AbstractEntity;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.plugin.api.model.BaseModel;
import ir.daneshrefah.scm.plugin.api.model.service.Service;
import ir.daneshrefah.scm.plugin.api.model.service.TransformerType;
import ir.daneshrefah.scm.plugin.api.model.service.composition.ServiceRelationType;
import jakarta.persistence.*;

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
    @Column(name = "TARGET_SERVICE_TRANSFORMER_REQUEST_TYPE_CODE")
    @Convert(converter = TransformerTypeConverter.class)
    private TransformerType targetServiceTransformerRequestType;
    private String targetServiceTransformerRequestMetadata;
    private String targetServiceTransformerRequestClassName;
    @Column(name = "TARGET_SERVICE_TRANSFORMER_RESPONSE_TYPE_CODE")
    @Convert(converter = TransformerTypeConverter.class)
    private TransformerType targetServiceTransformerResponseType;
    private String targetServiceTransformerResponseMetadata;
    private String targetServiceTransformerResponseClassName;

    @ManyToOne
    @JoinColumn(name = "TARGET_SERVICE_COMMIT_ID")
    private ServiceEntity targetServiceCommit;
    @Column(name = "TARGET_SERVICE_COMMIT_TRANSFORMER_REQUEST_TYPE_CODE")
    @Convert(converter = TransformerTypeConverter.class)
    private TransformerType targetServiceCommitTransformerRequestType;
    private String targetServiceCommitTransformerRequestMetadata;
    private String targetServiceCommitTransformerRequestClassName;
    @Column(name = "TARGET_SERVICE_COMMIT_TRANSFORMER_RESPONSE_TYPE_CODE")
    @Convert(converter = TransformerTypeConverter.class)
    private TransformerType targetServiceCommitTransformerResponseType;
    private String targetServiceCommitTransformerResponseMetadata;
    private String targetServiceCommitTransformerResponseClassName;

    @ManyToOne
    @JoinColumn(name = "TARGET_SERVICE_REVERSE_ID")
    private ServiceEntity targetServiceReverse;
    @Column(name = "TARGET_SERVICE_REVERSE_TRANSFORMER_REQUEST_TYPE_CODE")
    @Convert(converter = TransformerTypeConverter.class)
    private TransformerType targetServiceReverseTransformerRequestType;
    private String targetServiceReverseTransformerRequestMetadata;
    private String targetServiceReverseTransformerRequestClassName;
    @Column(name = "TARGET_SERVICE_REVERSE_TRANSFORMER_RESPONSE_TYPE_CODE")
    @Convert(converter = TransformerTypeConverter.class)
    private TransformerType targetServiceReverseTransformerResponseType;
    private String targetServiceReverseTransformerResponseMetadata;
    private String targetServiceReverseTransformerResponseClassName;


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

    public TransformerType getTargetServiceTransformerRequestType() {
        return targetServiceTransformerRequestType;
    }

    public void setTargetServiceTransformerRequestType(TransformerType targetServiceTransformerRequestType) {
        this.targetServiceTransformerRequestType = targetServiceTransformerRequestType;
    }

    public String getTargetServiceTransformerRequestMetadata() {
        return targetServiceTransformerRequestMetadata;
    }

    public void setTargetServiceTransformerRequestMetadata(String targetServiceTransformerRequestMetadata) {
        this.targetServiceTransformerRequestMetadata = targetServiceTransformerRequestMetadata;
    }

    public String getTargetServiceTransformerRequestClassName() {
        return targetServiceTransformerRequestClassName;
    }

    public void setTargetServiceTransformerRequestClassName(String targetServiceTransformerRequestClassName) {
        this.targetServiceTransformerRequestClassName = targetServiceTransformerRequestClassName;
    }

    public TransformerType getTargetServiceTransformerResponseType() {
        return targetServiceTransformerResponseType;
    }

    public void setTargetServiceTransformerResponseType(TransformerType targetServiceTransformerResponseType) {
        this.targetServiceTransformerResponseType = targetServiceTransformerResponseType;
    }

    public String getTargetServiceTransformerResponseMetadata() {
        return targetServiceTransformerResponseMetadata;
    }

    public void setTargetServiceTransformerResponseMetadata(String targetServiceTransformerResponseMetadata) {
        this.targetServiceTransformerResponseMetadata = targetServiceTransformerResponseMetadata;
    }

    public String getTargetServiceTransformerResponseClassName() {
        return targetServiceTransformerResponseClassName;
    }

    public void setTargetServiceTransformerResponseClassName(String targetServiceTransformerResponseClassName) {
        this.targetServiceTransformerResponseClassName = targetServiceTransformerResponseClassName;
    }

    public ServiceEntity getTargetServiceCommit() {
        return targetServiceCommit;
    }

    public void setTargetServiceCommit(ServiceEntity targetServiceCommit) {
        this.targetServiceCommit = targetServiceCommit;
    }

    public TransformerType getTargetServiceCommitTransformerRequestType() {
        return targetServiceCommitTransformerRequestType;
    }

    public void setTargetServiceCommitTransformerRequestType(TransformerType targetServiceCommitTransformerRequestType) {
        this.targetServiceCommitTransformerRequestType = targetServiceCommitTransformerRequestType;
    }

    public String getTargetServiceCommitTransformerRequestMetadata() {
        return targetServiceCommitTransformerRequestMetadata;
    }

    public void setTargetServiceCommitTransformerRequestMetadata(String targetServiceCommitTransformerRequestMetadata) {
        this.targetServiceCommitTransformerRequestMetadata = targetServiceCommitTransformerRequestMetadata;
    }

    public String getTargetServiceCommitTransformerRequestClassName() {
        return targetServiceCommitTransformerRequestClassName;
    }

    public void setTargetServiceCommitTransformerRequestClassName(String targetServiceCommitTransformerRequestClassName) {
        this.targetServiceCommitTransformerRequestClassName = targetServiceCommitTransformerRequestClassName;
    }

    public TransformerType getTargetServiceCommitTransformerResponseType() {
        return targetServiceCommitTransformerResponseType;
    }

    public void setTargetServiceCommitTransformerResponseType(TransformerType targetServiceCommitTransformerResponseType) {
        this.targetServiceCommitTransformerResponseType = targetServiceCommitTransformerResponseType;
    }

    public String getTargetServiceCommitTransformerResponseMetadata() {
        return targetServiceCommitTransformerResponseMetadata;
    }

    public void setTargetServiceCommitTransformerResponseMetadata(String targetServiceCommitTransformerResponseMetadata) {
        this.targetServiceCommitTransformerResponseMetadata = targetServiceCommitTransformerResponseMetadata;
    }

    public String getTargetServiceCommitTransformerResponseClassName() {
        return targetServiceCommitTransformerResponseClassName;
    }

    public void setTargetServiceCommitTransformerResponseClassName(String targetServiceCommitTransformerResponseClassName) {
        this.targetServiceCommitTransformerResponseClassName = targetServiceCommitTransformerResponseClassName;
    }

    public ServiceEntity getTargetServiceReverse() {
        return targetServiceReverse;
    }

    public void setTargetServiceReverse(ServiceEntity targetServiceReverse) {
        this.targetServiceReverse = targetServiceReverse;
    }

    public TransformerType getTargetServiceReverseTransformerRequestType() {
        return targetServiceReverseTransformerRequestType;
    }

    public void setTargetServiceReverseTransformerRequestType(TransformerType targetServiceReverseTransformerRequestType) {
        this.targetServiceReverseTransformerRequestType = targetServiceReverseTransformerRequestType;
    }

    public String getTargetServiceReverseTransformerRequestMetadata() {
        return targetServiceReverseTransformerRequestMetadata;
    }

    public void setTargetServiceReverseTransformerRequestMetadata(String targetServiceReverseTransformerRequestMetadata) {
        this.targetServiceReverseTransformerRequestMetadata = targetServiceReverseTransformerRequestMetadata;
    }

    public String getTargetServiceReverseTransformerRequestClassName() {
        return targetServiceReverseTransformerRequestClassName;
    }

    public void setTargetServiceReverseTransformerRequestClassName(String targetServiceReverseTransformerRequestClassName) {
        this.targetServiceReverseTransformerRequestClassName = targetServiceReverseTransformerRequestClassName;
    }

    public TransformerType getTargetServiceReverseTransformerResponseType() {
        return targetServiceReverseTransformerResponseType;
    }

    public void setTargetServiceReverseTransformerResponseType(TransformerType targetServiceReverseTransformerResponseType) {
        this.targetServiceReverseTransformerResponseType = targetServiceReverseTransformerResponseType;
    }

    public String getTargetServiceReverseTransformerResponseMetadata() {
        return targetServiceReverseTransformerResponseMetadata;
    }

    public void setTargetServiceReverseTransformerResponseMetadata(String targetServiceReverseTransformerResponseMetadata) {
        this.targetServiceReverseTransformerResponseMetadata = targetServiceReverseTransformerResponseMetadata;
    }

    public String getTargetServiceReverseTransformerResponseClassName() {
        return targetServiceReverseTransformerResponseClassName;
    }

    public void setTargetServiceReverseTransformerResponseClassName(String targetServiceReverseTransformerResponseClassName) {
        this.targetServiceReverseTransformerResponseClassName = targetServiceReverseTransformerResponseClassName;
    }
}
