package ir.daneshrefah.scm.common.dto.rest;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterDatasourceProperty;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class ResponseConditionDatasourceChangeRequest implements RequestData {

    private Long id;
    private ParameterDatasourceProperty property;
    private String value;
    private Integer length;
    private String convertorCode;
    private String conditionValue;
    private String operation;
    private LocalDateTime lastEditDate;

}
