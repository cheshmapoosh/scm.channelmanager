package ir.daneshrefah.scm.common.model.service.parameter;

import ir.daneshrefah.scm.common.AbstractAuditableModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParameterDatasourceCondition extends AbstractAuditableModel<Long> {
    private ParameterDatasource parameter;
    private String conditionValue;
    private DatasourceConditionOperation operation;
}
