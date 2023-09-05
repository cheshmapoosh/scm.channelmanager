package ir.daneshrefah.scm.core.entity.transformer;

import ir.daneshrefah.scm.common.model.transformer.TransformerRelationType;
import ir.daneshrefah.scm.core.converter.TransformerRelationTypeConverter;
import ir.daneshrefah.scm.core.entity.AbstractEntity;
import jakarta.persistence.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-08
 */
@Entity
@Table(name = "TBL_SCM_TRANSFORMER_RELATION")
public class TransformerRelationEntity extends AbstractEntity<String> {

    @Id
    @Column(name = "TRANSFORMER_RELATION_ID")
    private String id;
    @Column(name = "RELATION_TYPE_CODE")
    @Convert(converter = TransformerRelationTypeConverter.class)
    private TransformerRelationType relationType;
    @ManyToOne
    @JoinColumn(name = "TRANSFORMER_ID")
    private TransformerEntity transformer;
    private String sourceId;
    private String metadata;
    private Integer order;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public TransformerRelationType getRelationType() {
        return relationType;
    }

    public void setRelationType(TransformerRelationType relationType) {
        this.relationType = relationType;
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

    public TransformerEntity getTransformer() {
        return transformer;
    }

    public void setTransformer(TransformerEntity transformer) {
        this.transformer = transformer;
    }
}
