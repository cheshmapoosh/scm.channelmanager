package ir.daneshrefah.scm.common.model.service.parameter;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.model.transformer.Transformer;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-20
 */
@Data
public class ResponseCondition extends BaseModel<Integer> {

    private Map<ParameterDatasource, Object> conditions;
    private Transformer responseTransformer;
    private String responseExceptionErrorCodeProperty;
    private String responseExceptionErrorMessageProperty;
    private List<Parameter> responseParameters;

}
