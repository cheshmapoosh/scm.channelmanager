package ir.daneshrefah.scm.common.dto.rest;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterDatasourceProperty;
import ir.daneshrefah.scm.common.validation.NotBlankIfPresent;
import ir.daneshrefah.scm.common.validation.Numeric;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class ResponseConditionDatasourceChangeRequest implements RequestData {

    @NotNull
    @Numeric
    private Long id;
    @NotNull
    private ParameterDatasourceProperty property;
    private String value;
    @Numeric
    private Integer length;
    private String convertorCode;
    private String conditionValue;
    private String operation;
    @NotNull
    private LocalDateTime lastEditDate;

}
