package ir.daneshrefah.scm.core.entity.condition;

import ir.daneshrefah.scm.common.type.ConditionType;
import ir.daneshrefah.scm.common.type.PeriodType;
import ir.daneshrefah.scm.core.converter.PeriodTypeConverter;
import ir.daneshrefah.scm.core.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "TBL_SCM_CONDITION")
@Setter
@Getter
public class ConditionEntity extends AbstractEntity<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "CONDITION_ID")
    private String id;
    @Column(name = "TITLE")
    private String title;
    @Column(name = "DESC")
    private String desc;
    @Column(name = "TYPE")
    @Enumerated(EnumType.ORDINAL)
    private ConditionType type;
    @Column(name = "VALUE")
    private Long value;
    @Column(name = "PERIOD_TYPE")
    @Convert(converter = PeriodTypeConverter.class)
    private PeriodType periodType;
    @Column(name = "PERIOD_VALUE")
    private Integer periodValue;

}
