package ir.daneshrefah.scm.common.model.condition;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.type.ConditionType;
import ir.daneshrefah.scm.common.type.PeriodType;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class Condition extends BaseModel<Long> {

    private Long id;
    private String title;
    private String desc;
    private ConditionType type;
    private String value;
    private PeriodType periodType;
    private Integer periodValue;
    /**
    * this property determine that this condition could be overwritten by conditions that has 'true' bypassIgnorable
    * */
    private boolean ignorable;
    private boolean bypassIgnorable;

}
