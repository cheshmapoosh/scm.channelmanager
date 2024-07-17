package ir.daneshrefah.scm.plugin.api.model.service.external.parameter;

import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-14
 */
@Data
public class ParameterDatasource {

    private ParameterDatasourceOriginType originType;
    private String parameter;
    private String convertorCode;

}
