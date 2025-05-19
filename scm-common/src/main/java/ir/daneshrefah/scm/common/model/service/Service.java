package ir.daneshrefah.scm.common.model.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ir.daneshrefah.scm.common.AbstractStringAuditableModel;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterActionType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
@Getter
@Setter
public abstract class Service extends AbstractStringAuditableModel<String> {

    private String code;
    private String title;
    private String alias;
    private Integer version;
    private Boolean isSystemic;
    private ServiceType type;
    private ServiceStatus status;
    private Service parent;
    private ServiceImplementationType implementationType;
    private String requestJsonSchema;
    private String responseJsonSchema;
    private Boolean checkAccessFirstAuthentication;
    private Boolean checkAccessSecondAuthentication;
    private Boolean checkAccessService;
    private Boolean checkAccessAsset;
    private String amountProperty;
    private String assetProperty;
    private transient boolean proxy;
    private transient String targetProxyCode;
    @JsonIgnore
    private List<Parameter> parameters;

    public List<Parameter> getParameters(ParameterActionType actionType){
        return getParameters()
                .stream()
                .filter(parameter -> actionType.equals(parameter.getActionType()))
                .toList();
    }

}
