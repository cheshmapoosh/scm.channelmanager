package ir.daneshrefah.scm.common.model.service.parameter;

import ir.daneshrefah.scm.common.BaseModel;
import lombok.Data;

@Data
public class ParameterDatasourceCondition extends BaseModel<Long> {
    private ParameterDatasource parameter;
    private String conditionValue;
}
