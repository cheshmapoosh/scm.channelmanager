package ir.daneshrefah.scm.plugin.api.model.service.composition;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelation;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-08
 */
public class ServiceRelation extends BaseModel<String> {

    private Service sourceService;
    private Integer order;
    private ServiceRelationType relationType;
    private Service targetService;
    private List<TransformerRelation> targetServiceRequestTransformers;
    private List<TransformerRelation> targetServiceResponseTransformers;
    private Service targetServiceCommit;
    private List<TransformerRelation> targetServiceCommitRequestTransformers;
    private List<TransformerRelation> targetServiceCommitResponseTransformers;
    private Service targetServiceReverse;
    private List<TransformerRelation> targetServiceReverseRequestTransformers;
    private List<TransformerRelation> targetServiceReverseResponseTransformers;

    public Service getSourceService() {
        return sourceService;
    }

    public void setSourceService(Service sourceService) {
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

    public Service getTargetService() {
        return targetService;
    }

    public void setTargetService(Service targetService) {
        this.targetService = targetService;
    }

    public List<TransformerRelation> getTargetServiceRequestTransformers() {
        return targetServiceRequestTransformers;
    }

    public void setTargetServiceRequestTransformers(List<TransformerRelation> targetServiceRequestTransformers) {
        this.targetServiceRequestTransformers = targetServiceRequestTransformers;
    }

    public List<TransformerRelation> getTargetServiceResponseTransformers() {
        return targetServiceResponseTransformers;
    }

    public void setTargetServiceResponseTransformers(List<TransformerRelation> targetServiceResponseTransformers) {
        this.targetServiceResponseTransformers = targetServiceResponseTransformers;
    }

    public Service getTargetServiceCommit() {
        return targetServiceCommit;
    }

    public void setTargetServiceCommit(Service targetServiceCommit) {
        this.targetServiceCommit = targetServiceCommit;
    }

    public List<TransformerRelation> getTargetServiceCommitRequestTransformers() {
        return targetServiceCommitRequestTransformers;
    }

    public void setTargetServiceCommitRequestTransformers(List<TransformerRelation> targetServiceCommitRequestTransformers) {
        this.targetServiceCommitRequestTransformers = targetServiceCommitRequestTransformers;
    }

    public List<TransformerRelation> getTargetServiceCommitResponseTransformers() {
        return targetServiceCommitResponseTransformers;
    }

    public void setTargetServiceCommitResponseTransformers(List<TransformerRelation> targetServiceCommitResponseTransformers) {
        this.targetServiceCommitResponseTransformers = targetServiceCommitResponseTransformers;
    }

    public Service getTargetServiceReverse() {
        return targetServiceReverse;
    }

    public void setTargetServiceReverse(Service targetServiceReverse) {
        this.targetServiceReverse = targetServiceReverse;
    }

    public List<TransformerRelation> getTargetServiceReverseRequestTransformers() {
        return targetServiceReverseRequestTransformers;
    }

    public void setTargetServiceReverseRequestTransformers(List<TransformerRelation> targetServiceReverseRequestTransformers) {
        this.targetServiceReverseRequestTransformers = targetServiceReverseRequestTransformers;
    }

    public List<TransformerRelation> getTargetServiceReverseResponseTransformers() {
        return targetServiceReverseResponseTransformers;
    }

    public void setTargetServiceReverseResponseTransformers(List<TransformerRelation> targetServiceReverseResponseTransformers) {
        this.targetServiceReverseResponseTransformers = targetServiceReverseResponseTransformers;
    }
}
