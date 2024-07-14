package ir.daneshrefah.scm.core.entity.service.parameter;

import ir.daneshrefah.scm.plugin.api.model.service.external.parameter.ParameterDatasource;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-14
 */
@Data
public class ParameterEntity {

    private String name;
    private ParameterDatasource datasource;
    private ParameterEntity parent;

}
