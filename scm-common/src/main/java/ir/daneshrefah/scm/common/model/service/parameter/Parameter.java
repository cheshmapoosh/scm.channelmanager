package ir.daneshrefah.scm.common.model.service.parameter;

import ir.daneshrefah.scm.common.AbstractAuditableModel;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-17
 */
@Getter
@Setter
public class Parameter extends AbstractAuditableModel<String> {

    private String name;
    private ParameterDatasource datasource;
    private ParameterType type;
    private boolean required;
    private String tag;
    private String title;


    private Integer order;
    /**
     * If action type was proxy , the parent id
     * is target of proxy and the datasource of
     * proxy parameter replaced with parent vales
     */
    private Parameter parent;
    private ParameterActionType actionType;
    private String defaultValue;

}
