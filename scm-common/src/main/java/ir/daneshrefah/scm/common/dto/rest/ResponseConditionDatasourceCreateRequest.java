package ir.daneshrefah.scm.common.dto.rest;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.service.parameter.DatasourceConditionOperation;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterDatasourceProperty;
import ir.daneshrefah.scm.common.validation.NotBlankIfPresent;
import ir.daneshrefah.scm.common.validation.Numeric;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResponseConditionDatasourceCreateRequest implements RequestData {

    @NotNull
    @NotBlank
    private String responseId;
    @NotNull
    private ParameterDatasourceProperty property;
    @NotBlankIfPresent
    private String value;
    @Numeric
    private Integer length;
    @NotBlankIfPresent
    private String convertorCode;
    @NotBlankIfPresent
    private String conditionValue;
    @NotNull
    private DatasourceConditionOperation operation;

}
