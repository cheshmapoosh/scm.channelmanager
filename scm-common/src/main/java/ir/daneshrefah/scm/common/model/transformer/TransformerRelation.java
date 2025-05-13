package ir.daneshrefah.scm.common.model.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.AbstractAuditableModel;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-08
 */
@Getter
@Setter
public class TransformerRelation extends AbstractAuditableModel<Long> {

    private TransformerRelationType relationType;
    private Transformer transformer;
    private String sourceId;
    private JsonNode metadata;
    private Integer order;

}
