package ir.daneshrefah.scm.plugin.api.model.service.composition;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.TransformerType;

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
    private TransformerType targetServiceTransformerRequestType;
    private String targetServiceTransformerRequestMetadata;
    private String targetServiceTransformerRequestClassName;
    private TransformerType targetServiceTransformerResponseType;
    private String targetServiceTransformerResponseMetadata;
    private String targetServiceTransformerResponseClassName;

    private Service targetServiceCommit;
    private TransformerType targetServiceCommitTransformerRequestType;
    private String targetServiceCommitTransformerRequestMetadata;
    private String targetServiceCommitTransformerRequestClassName;
    private TransformerType targetServiceCommitTransformerResponseType;
    private String targetServiceCommitTransformerResponseMetadata;
    private String targetServiceCommitTransformerResponseClassName;

    private Service targetServiceReverse;
    private TransformerType targetServiceReverseTransformerRequestType;
    private String targetServiceReverseTransformerRequestMetadata;
    private String targetServiceReverseTransformerRequestClassName;
    private TransformerType targetServiceReverseTransformerResponseType;
    private String targetServiceReverseTransformerResponseMetadata;
    private String targetServiceReverseTransformerResponseClassName;

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

    public Service getTargetServiceCommit() {
        return targetServiceCommit;
    }

    public void setTargetServiceCommit(Service targetServiceCommit) {
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

    public Service getTargetServiceReverse() {
        return targetServiceReverse;
    }

    public void setTargetServiceReverse(Service targetServiceReverse) {
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
