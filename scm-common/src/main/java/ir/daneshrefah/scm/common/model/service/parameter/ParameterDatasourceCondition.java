package ir.daneshrefah.scm.common.model.service.parameter;

import ir.daneshrefah.scm.common.AuditableModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParameterDatasourceCondition extends AuditableModel<Long> {
    private ParameterDatasource parameter;
    private String conditionValue;
    private DatasourceConditionOperation operation;
}
