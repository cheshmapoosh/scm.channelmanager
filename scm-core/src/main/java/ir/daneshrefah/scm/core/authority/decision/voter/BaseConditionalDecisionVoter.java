package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.common.type.ConditionType;
import ir.daneshrefah.scm.core.authority.decision.helper.DecisionHelper;
import ir.daneshrefah.scm.core.model.condition.Condition;
import lombok.RequiredArgsConstructor;

import java.util.Iterator;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-09
 */
@RequiredArgsConstructor
public abstract class BaseConditionalDecisionVoter extends DecisionVoter {

    protected final DecisionHelper decisionHelper;

    @Override
    protected int vote(Message message) {
        ConditionType conditionType = getConditionType();
        List<Condition> userConditions = decisionHelper.findUserConditions(conditionType,
                message.getHeader().getTerminalCode(), message.getHeader().getAuthentication());
        int result = checkConditions(message, userConditions);
        if (ACCESS_ABSTAIN != result) {
            return result;
        }

        List<Condition> terminalConditions = decisionHelper.findTerminalConditions(conditionType,
                message.getHeader().getServiceAccess(), message.getHeader().getAuthentication());
        return checkConditions(message, terminalConditions);
    }

    private int checkConditions(Message message, List<Condition> conditions) {
        for (Iterator<Condition> iterator = conditions.iterator(); iterator.hasNext(); ) {
            Condition condition = iterator.next();
            int conditionResult = checkCondition(message, condition);
            if (ACCESS_ABSTAIN != conditionResult) {
                return conditionResult;
            }
        }
        return ACCESS_ABSTAIN;
    }

    protected abstract int checkCondition(Message message, Condition condition);

    protected abstract ConditionType getConditionType();

    @Override
    protected boolean support(TerminalServiceAccess serviceAccess) {
        return true;
    }

}
