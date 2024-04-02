package ir.daneshrefah.scm.common.model.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.BaseModel;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-08
 */
@Data
public class TransformerRelation extends BaseModel<String> {

    private TransformerRelationType relationType;
    private Transformer transformer;
    private String sourceId;
    private JsonNode metadata;
    private Integer order;

}
