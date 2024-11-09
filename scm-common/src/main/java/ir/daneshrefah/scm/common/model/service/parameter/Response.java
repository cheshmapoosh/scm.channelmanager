package ir.daneshrefah.scm.common.model.service.parameter;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.model.service.ExternalServiceBodyType;
import ir.daneshrefah.scm.common.model.transformer.Transformer;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Response extends BaseModel<String> {

    private List<ParameterDatasourceCondition> conditions;
    private Transformer responseTransformer;
    private String responseErrorCodeProperty;
    private String responseErrorMessageProperty;
    private List<Parameter> responseParameters;
    private ExternalServiceBodyType responseBodyType;
    private boolean enable;
    private String title;

}
