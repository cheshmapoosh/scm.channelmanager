package ir.daneshrefah.scm.core.integration.service;

import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelation;
import ir.daneshrefah.scm.plugin.api.model.service.composition.ServiceRelation;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-09-05
 */
public class CompositeServiceExecutionWrapper {

    private ServiceRelation serviceRelation;
    private List<TransformerExecutionWrapper> targetServiceRequestTransformers;
    private List<TransformerExecutionWrapper> targetServiceResponseTransformers;
    private List<TransformerExecutionWrapper> targetServiceCommitRequestTransformers;
    private List<TransformerExecutionWrapper> targetServiceCommitResponseTransformers;
    private List<TransformerExecutionWrapper> targetServiceReverseRequestTransformers;
    private List<TransformerExecutionWrapper> targetServiceReverseResponseTransformers;

    public CompositeServiceExecutionWrapper(ServiceRelation serviceRelation) {
        this.serviceRelation = serviceRelation;
    }

    public ServiceRelation getServiceRelation() {
        return serviceRelation;
    }

    public void setServiceRelation(ServiceRelation serviceRelation) {
        this.serviceRelation = serviceRelation;
    }

    public List<TransformerExecutionWrapper> getTargetServiceRequestTransformers() {
        return targetServiceRequestTransformers;
    }

    public void setTargetServiceRequestTransformers(List<TransformerExecutionWrapper> targetServiceRequestTransformers) {
        this.targetServiceRequestTransformers = targetServiceRequestTransformers;
    }

    public List<TransformerExecutionWrapper> getTargetServiceResponseTransformers() {
        return targetServiceResponseTransformers;
    }

    public void setTargetServiceResponseTransformers(List<TransformerExecutionWrapper> targetServiceResponseTransformers) {
        this.targetServiceResponseTransformers = targetServiceResponseTransformers;
    }

    public List<TransformerExecutionWrapper> getTargetServiceCommitRequestTransformers() {
        return targetServiceCommitRequestTransformers;
    }

    public void setTargetServiceCommitRequestTransformers(List<TransformerExecutionWrapper> targetServiceCommitRequestTransformers) {
        this.targetServiceCommitRequestTransformers = targetServiceCommitRequestTransformers;
    }

    public List<TransformerExecutionWrapper> getTargetServiceCommitResponseTransformers() {
        return targetServiceCommitResponseTransformers;
    }

    public void setTargetServiceCommitResponseTransformers(List<TransformerExecutionWrapper> targetServiceCommitResponseTransformers) {
        this.targetServiceCommitResponseTransformers = targetServiceCommitResponseTransformers;
    }

    public List<TransformerExecutionWrapper> getTargetServiceReverseRequestTransformers() {
        return targetServiceReverseRequestTransformers;
    }

    public void setTargetServiceReverseRequestTransformers(List<TransformerExecutionWrapper> targetServiceReverseRequestTransformers) {
        this.targetServiceReverseRequestTransformers = targetServiceReverseRequestTransformers;
    }

    public List<TransformerExecutionWrapper> getTargetServiceReverseResponseTransformers() {
        return targetServiceReverseResponseTransformers;
    }

    public void setTargetServiceReverseResponseTransformers(List<TransformerExecutionWrapper> targetServiceReverseResponseTransformers) {
        this.targetServiceReverseResponseTransformers = targetServiceReverseResponseTransformers;
    }
}
