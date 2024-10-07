package ir.daneshrefah.scm.common.service.rest;

import ir.daneshrefah.scm.common.dto.RequestData;
import ir.daneshrefah.scm.common.model.service.parameter.DatasourceConditionOperation;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterDatasourceProperty;
import lombok.Data;

@Data
public class ResponseConditionDatasourceRequest implements RequestData {

    private String responseConditionId;
    private ParameterDatasourceProperty property;
    private String value;
    private Integer length;
    private String convertorCode;
    private String conditionValue;
    private DatasourceConditionOperation operation;

}
