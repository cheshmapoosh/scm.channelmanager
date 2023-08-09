package ir.daneshrefah.scm.plugin.api.model.service.composition;

import ir.daneshrefah.scm.plugin.api.model.BaseModel;
import ir.daneshrefah.scm.plugin.api.model.service.Service;
import ir.daneshrefah.scm.plugin.api.model.service.TransformerType;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-08
 */
public class ServiceRelation extends BaseModel<String> {

    private Service sourceService;
    private Service targetService;
    private Service targetSubmitService;
    private Service targetReverseService;
    private ServiceRelationType relationType;

    private TransformerType requestTransformerType;
    private TransformerType responseTransformerType;
    private String requestTransformMetadata;
    private String responseTransformMetadata;
    private String requestTransformerClass;
    private String responseTransformerClass;

    private TransformerType requestSubmitTransformerType;
    private TransformerType responseSubmitTransformerType;
    private String requestSubmitTransformMetadata;
    private String responseSubmitTransformMetadata;
    private String requestSubmitTransformerClass;
    private String responseSubmitTransformerClass;

    private TransformerType requestReverseTransformerType;
    private TransformerType responseReverseTransformerType;
    private String requestReverseTransformMetadata;
    private String responseReverseTransformMetadata;
    private String requestReverseTransformerClass;
    private String responseReverseTransformerClass;

    public Service getSourceService() {
        return sourceService;
    }

    public void setSourceService(Service sourceService) {
        this.sourceService = sourceService;
    }

    public Service getTargetService() {
        return targetService;
    }

    public void setTargetService(Service targetService) {
        this.targetService = targetService;
    }

    public Service getTargetSubmitService() {
        return targetSubmitService;
    }

    public void setTargetSubmitService(Service targetSubmitService) {
        this.targetSubmitService = targetSubmitService;
    }

    public Service getTargetReverseService() {
        return targetReverseService;
    }

    public void setTargetReverseService(Service targetReverseService) {
        this.targetReverseService = targetReverseService;
    }

    public ServiceRelationType getRelationType() {
        return relationType;
    }

    public void setRelationType(ServiceRelationType relationType) {
        this.relationType = relationType;
    }

    public TransformerType getRequestTransformerType() {
        return requestTransformerType;
    }

    public void setRequestTransformerType(TransformerType requestTransformerType) {
        this.requestTransformerType = requestTransformerType;
    }

    public TransformerType getResponseTransformerType() {
        return responseTransformerType;
    }

    public void setResponseTransformerType(TransformerType responseTransformerType) {
        this.responseTransformerType = responseTransformerType;
    }

    public String getRequestTransformMetadata() {
        return requestTransformMetadata;
    }

    public void setRequestTransformMetadata(String requestTransformMetadata) {
        this.requestTransformMetadata = requestTransformMetadata;
    }

    public String getResponseTransformMetadata() {
        return responseTransformMetadata;
    }

    public void setResponseTransformMetadata(String responseTransformMetadata) {
        this.responseTransformMetadata = responseTransformMetadata;
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

    public TransformerType getRequestSubmitTransformerType() {
        return requestSubmitTransformerType;
    }

    public void setRequestSubmitTransformerType(TransformerType requestSubmitTransformerType) {
        this.requestSubmitTransformerType = requestSubmitTransformerType;
    }

    public TransformerType getResponseSubmitTransformerType() {
        return responseSubmitTransformerType;
    }

    public void setResponseSubmitTransformerType(TransformerType responseSubmitTransformerType) {
        this.responseSubmitTransformerType = responseSubmitTransformerType;
    }

    public String getRequestSubmitTransformMetadata() {
        return requestSubmitTransformMetadata;
    }

    public void setRequestSubmitTransformMetadata(String requestSubmitTransformMetadata) {
        this.requestSubmitTransformMetadata = requestSubmitTransformMetadata;
    }

    public String getResponseSubmitTransformMetadata() {
        return responseSubmitTransformMetadata;
    }

    public void setResponseSubmitTransformMetadata(String responseSubmitTransformMetadata) {
        this.responseSubmitTransformMetadata = responseSubmitTransformMetadata;
    }

    public String getRequestSubmitTransformerClass() {
        return requestSubmitTransformerClass;
    }

    public void setRequestSubmitTransformerClass(String requestSubmitTransformerClass) {
        this.requestSubmitTransformerClass = requestSubmitTransformerClass;
    }

    public String getResponseSubmitTransformerClass() {
        return responseSubmitTransformerClass;
    }

    public void setResponseSubmitTransformerClass(String responseSubmitTransformerClass) {
        this.responseSubmitTransformerClass = responseSubmitTransformerClass;
    }

    public TransformerType getRequestReverseTransformerType() {
        return requestReverseTransformerType;
    }

    public void setRequestReverseTransformerType(TransformerType requestReverseTransformerType) {
        this.requestReverseTransformerType = requestReverseTransformerType;
    }

    public TransformerType getResponseReverseTransformerType() {
        return responseReverseTransformerType;
    }

    public void setResponseReverseTransformerType(TransformerType responseReverseTransformerType) {
        this.responseReverseTransformerType = responseReverseTransformerType;
    }

    public String getRequestReverseTransformMetadata() {
        return requestReverseTransformMetadata;
    }

    public void setRequestReverseTransformMetadata(String requestReverseTransformMetadata) {
        this.requestReverseTransformMetadata = requestReverseTransformMetadata;
    }

    public String getResponseReverseTransformMetadata() {
        return responseReverseTransformMetadata;
    }

    public void setResponseReverseTransformMetadata(String responseReverseTransformMetadata) {
        this.responseReverseTransformMetadata = responseReverseTransformMetadata;
    }

    public String getRequestReverseTransformerClass() {
        return requestReverseTransformerClass;
    }

    public void setRequestReverseTransformerClass(String requestReverseTransformerClass) {
        this.requestReverseTransformerClass = requestReverseTransformerClass;
    }

    public String getResponseReverseTransformerClass() {
        return responseReverseTransformerClass;
    }

    public void setResponseReverseTransformerClass(String responseReverseTransformerClass) {
        this.responseReverseTransformerClass = responseReverseTransformerClass;
    }
}
