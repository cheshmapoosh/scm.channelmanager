package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.type.ConditionType;
import ir.daneshrefah.scm.core.authority.decision.helper.DecisionHelper;
import ir.daneshrefah.scm.common.model.condition.Condition;
import ir.daneshrefah.scm.common.model.condition.ConditionKey;

import java.util.*;

import static ir.daneshrefah.scm.utils.string.StringUtils.isNumeric;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-09
 */
public class WithdrawConditionalDecisionVoter extends BaseConditionalDecisionVoter {

    public WithdrawConditionalDecisionVoter(DecisionHelper decisionHelper) {
        super(decisionHelper);
    }

    @Override
    protected int checkConditions(Message message, List<Condition> conditions) {
        if (Objects.isNull(conditions) || conditions.isEmpty()) {
            return ACCESS_ABSTAIN;
        }
        boolean isGrantedByPowerCondition = false;
        Map<ConditionKey, Condition> conditionMap = new HashMap<>();
        for (Iterator<Condition> iterator = conditions.iterator(); iterator.hasNext(); ) {
            Condition condition = iterator.next();
            if (isGrantedByPowerCondition && condition.isIgnorable()) {
                continue;
            }
            ConditionKey key = new ConditionKey(condition.getType(), condition.getPeriodType(), condition.getPeriodValue());
            Condition existCondition = conditionMap.get(key);
            long existValue = Objects.nonNull(existCondition) && isNumeric(existCondition.getValue()) ? Long.valueOf(existCondition.getValue()) : 0;
            long currentValue = isNumeric(condition.getValue()) ? Long.valueOf(condition.getValue()) : 0;
            if (existValue < currentValue) {
                conditionMap.put(key, condition);
            }
            isGrantedByPowerCondition = isGrantedByPowerCondition || condition.isBypassIgnorable();
        }
        message.getHeader().setWithdrawConditions(conditionMap);
        return ACCESS_ABSTAIN;
    }

    @Override
    protected ConditionType getConditionType() {
        return ConditionType.WITHDRAW;
    }

}
