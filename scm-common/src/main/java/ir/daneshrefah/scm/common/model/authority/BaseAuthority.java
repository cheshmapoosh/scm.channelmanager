package ir.daneshrefah.scm.common.model.authority;


import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;

import java.util.regex.Pattern;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public abstract class BaseAuthority extends BaseModel implements Authority {

    private String condition;
    private Pattern conditionPattern;

    public abstract AuthorityType getType();

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
        if (null != condition) {
            conditionPattern = Pattern.compile(condition);
        }
    }

    public Pattern getConditionPattern() {
        return conditionPattern;
    }

    public void setConditionPattern(Pattern conditionPattern) {
        this.conditionPattern = conditionPattern;
    }

    public boolean isGranted(TerminalServiceChannelAccess service, Message message) {
        return true;
    }

}
