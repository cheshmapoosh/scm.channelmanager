package ir.daneshrefah.scm.core.entity.service.parameter;

import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import ir.daneshrefah.scm.common.data.entity.AbstractVersionAbleDefaultEntity;
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
    private Long id;
    @Embedded
    private ParameterDatasourceEntity parameter;
    private String conditionValue;
}
