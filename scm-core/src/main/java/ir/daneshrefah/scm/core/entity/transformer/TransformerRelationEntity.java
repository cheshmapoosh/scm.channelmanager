package ir.daneshrefah.scm.core.entity.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.transformer.TransformerRelationType;
import ir.daneshrefah.scm.common.data.converter.JsonNodeTypeConverter;
import ir.daneshrefah.scm.core.converter.TransformerRelationTypeConverter;
import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import jakarta.persistence.*;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-08
 */
@Data
@Entity
@Table(name = "TBL_SCM_TRANSFORMER_RELATION")
public class TransformerRelationEntity extends AbstractDefaultEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRANSFORMER_RELATION_ID")
    private Long id;
    @Column(name = "RELATION_TYPE_CODE")
    @Convert(converter = TransformerRelationTypeConverter.class)
    private TransformerRelationType relationType;
    @ManyToOne
    @JoinColumn(name = "TRANSFORMER_ID")
    private TransformerEntity transformer;
    private String sourceId;
    @Convert(converter = JsonNodeTypeConverter.class)
    private JsonNode metadata;
    private Integer order;
}
