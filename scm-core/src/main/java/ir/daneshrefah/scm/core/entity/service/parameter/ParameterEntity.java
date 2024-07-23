package ir.daneshrefah.scm.core.entity.service.parameter;

import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterDatasource;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterType;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-14
 */
@Data
public class ParameterEntity extends AbstractDefaultEntity<Long> {

    private Long id;
    private String name;
    private ParameterDatasource datasource;
    private ParameterType type;
    private ParameterEntity parent;

}
