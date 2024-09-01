package ir.daneshrefah.scm.common.model.service.parameter;

import lombok.Data;

import java.io.Serializable;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-14
 */
@Data
public class ParameterDatasource implements Serializable {

    private ParameterDatasourceProperty property;
    private String value;
    private Integer length;
    private String convertorCode;

}
