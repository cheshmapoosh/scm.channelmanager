package ir.daneshrefah.scm.common.service.rest;

import ir.daneshrefah.scm.common.dto.RequestData;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterActionType;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterDatasourceProperty;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterType;
import lombok.Data;

@Data
public class ParameterCreateRequest implements RequestData {
    private String parameterName;
    private String title;
    //Datasource
    private ParameterDatasourceProperty datasourcePropertyType;
    private String value;
    private Integer length;
    private String convertorCode;
    //
    private ParameterType parameterType;
    private boolean required;
    private String tag;
    private Integer order;
    private String parentId;
    private ParameterActionType actionType;
    private String defaultValue;
    //Parameter target usage
    private String serviceProviderId;
    private String serviceId;
    private String responseId;

}
