package ir.daneshrefah.scm.common.model.authority.terminal;

import ir.daneshrefah.scm.common.model.authority.Authority;
import ir.daneshrefah.scm.common.model.authority.AuthorityType;
import ir.daneshrefah.scm.common.type.DurationType;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-14
 */
public class TerminalWithdrawAuthority extends Authority {

    private DurationType withdrawDurationType;
    private Integer withdrawDuration;
    private Integer withdrawMinAmount;
    private Integer withdrawMaxAmount;

    @Override
    public AuthorityType getAuthorityType() {
        return AuthorityType.TERMINAL_WITHDRAW;
    }

    public DurationType getWithdrawDurationType() {
        return withdrawDurationType;
    }

    public void setWithdrawDurationType(DurationType withdrawDurationType) {
        this.withdrawDurationType = withdrawDurationType;
    }

    public Integer getWithdrawDuration() {
        return withdrawDuration;
    }

    public void setWithdrawDuration(Integer withdrawDuration) {
        this.withdrawDuration = withdrawDuration;
    }

    public Integer getWithdrawMinAmount() {
        return withdrawMinAmount;
    }

    public void setWithdrawMinAmount(Integer withdrawMinAmount) {
        this.withdrawMinAmount = withdrawMinAmount;
    }

    public Integer getWithdrawMaxAmount() {
        return withdrawMaxAmount;
    }

    public void setWithdrawMaxAmount(Integer withdrawMaxAmount) {
        this.withdrawMaxAmount = withdrawMaxAmount;
    }
}
