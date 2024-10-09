package ir.daneshrefah.scm.core.entity.service.parameter;

import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import ir.daneshrefah.scm.common.data.entity.AbstractVersionAbleDefaultEntity;
import ir.daneshrefah.scm.common.model.service.parameter.DatasourceConditionOperation;
import ir.daneshrefah.scm.core.converter.DatasourceConditionOperationConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "TBL_SCM_DATASOURCE_CONDITION")
@Setter
@Getter
public class ParameterDatasourceConditionEntity extends AbstractVersionAbleDefaultEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DATASOURCE_CONDITION_ID")
    private Long id;
    @Embedded
    private ParameterDatasourceEntity parameter;
    private String conditionValue;
    @Column(name = "OPERATION")
    @Convert(converter = DatasourceConditionOperationConverter.class)
    private DatasourceConditionOperation operation;
}
