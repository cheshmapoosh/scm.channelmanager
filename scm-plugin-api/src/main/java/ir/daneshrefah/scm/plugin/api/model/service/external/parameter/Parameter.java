package ir.daneshrefah.scm.plugin.api.model.service.external.parameter;

import ir.daneshrefah.scm.common.BaseModel;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-17
 */
@Data
public class Parameter extends BaseModel<Long> {

    private Long id;
    private String name;
    private ParameterDatasource datasource;
    private ParameterType type;
    private Parameter parent;

}
