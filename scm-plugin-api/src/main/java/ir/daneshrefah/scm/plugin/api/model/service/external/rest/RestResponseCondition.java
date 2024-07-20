package ir.daneshrefah.scm.plugin.api.model.service.external.rest;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.plugin.api.model.service.external.parameter.Parameter;
import lombok.Data;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-20
 */
@Data
public class RestResponseCondition extends BaseModel<Integer> {

    private Integer responseStatusCode;
    private String propertyShouldExist;
    private String propertyShouldValue;
    private List<Parameter> parameters;

}
