package ir.daneshrefah.scm.common.model.transformer;

import ir.daneshrefah.scm.common.BaseModel;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-08
 */
public class TransformerRelation extends BaseModel<String> {

    private TransformerRelationType relationType;
    private Transformer transformer;
    private String sourceId;
    private String metadata;
    private Integer order;

    public TransformerRelationType getRelationType() {
        return relationType;
    }

    public void setRelationType(TransformerRelationType relationType) {
        this.relationType = relationType;
    }

    public Transformer getTransformer() {
        return transformer;
    }

    public void setTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}
