package ir.daneshrefah.scm.common.service.rest;

import ir.daneshrefah.scm.common.dto.RequestData;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterActionType;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterDatasourceProperty;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ParameterChangeRequest implements RequestData {

    private String id;
    private String parameterName;
    //Datasource
    private ParameterDatasourceProperty datasourcePropertyType;
    private String value;
    private Integer length;
    private String convertorCode;
    //
    private ParameterType parameterType;
    private Boolean required;
    private Boolean internal;
    private String tag;
    private Integer order;
    private String parentId;
    private ParameterActionType actionType;
    private String defaultValue;
    private LocalDateTime lastEditDate;

}
