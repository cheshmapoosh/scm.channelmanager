package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.core.authority.decision.helper.DecisionHelper;
import ir.daneshrefah.scm.common.model.condition.Condition;

import java.util.Iterator;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-09
 */
public abstract class BaseSingularConditionalDecisionVoter extends BaseConditionalDecisionVoter {

    public BaseSingularConditionalDecisionVoter(DecisionHelper decisionHelper) {
        super(decisionHelper);
    }

    @Override
    protected final int checkConditions(Message message, List<Condition> conditions) {
        boolean isGrantedByPowerCondition = false;
        for (Iterator<Condition> iterator = conditions.iterator(); iterator.hasNext(); ) {
            Condition condition = iterator.next();
            int conditionResult = isGrantedByPowerCondition && condition.isIgnorable() ? ACCESS_ABSTAIN : checkCondition(message, condition);
            if (ACCESS_DENIED == conditionResult) {
                return conditionResult;
            }
            if (ACCESS_GRANTED == conditionResult && condition.isBypassIgnorable()) {
                isGrantedByPowerCondition = true;
            }
        }
        return ACCESS_ABSTAIN;
    }

    protected abstract int checkCondition(Message message, Condition condition);

}
