package ir.daneshrefah.scm.common.model.condition;

import ir.daneshrefah.scm.common.type.ConditionType;
import ir.daneshrefah.scm.common.type.PeriodType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-09
 */
@RequiredArgsConstructor
@Getter
public class ConditionKey implements Serializable {

    private final ConditionType type;
    private final PeriodType periodType;
    private final Integer periodValue;

    @Override
    public boolean equals(Object obj) {
        if (!ConditionKey.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        return null != periodType && null != periodValue &&
                periodType.equals(((ConditionKey) obj).getPeriodType()) &&
                periodValue.equals(((ConditionKey) obj).getPeriodValue());
    }
}
