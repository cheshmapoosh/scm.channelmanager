package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.type.ConditionType;
import ir.daneshrefah.scm.core.authority.decision.helper.DecisionHelper;
import ir.daneshrefah.scm.core.model.condition.Condition;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-09
 */
public class AuthorityConditionalDecisionVoter extends BaseConditionalDecisionVoter {

    public AuthorityConditionalDecisionVoter(DecisionHelper decisionHelper) {
        super(decisionHelper);
    }

    @Override
    protected int checkCondition(Message message, Condition condition) {
        return ACCESS_ABSTAIN;
    }

    @Override
    protected ConditionType getConditionType() {
        return ConditionType.AUTHORITY;
    }
}
