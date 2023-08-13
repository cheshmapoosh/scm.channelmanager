package ir.daneshrefah.scm.core.entity.authority;

import ir.daneshrefah.scm.common.type.DurationType;
import ir.daneshrefah.scm.core.converter.DurationTypeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-13
 */
@Entity
@DiscriminatorValue("2")
public class TerminalWithdrawAuthorityEntity extends AuthorityEntity {

    @Column(name = "TARGET_WITHDRAW_DURATION_TYPE_CODE", insertable = false, updatable = false)
    @Convert(converter = DurationTypeConverter.class)
    private DurationType withdrawDurationType;
    @Column(name = "TARGET_WITHDRAW_DURATION", insertable = false, updatable = false)
    private Integer withdrawDuration;
    @Column(name = "TARGET_WITHDRAW_MIN_AMOUNT", insertable = false, updatable = false)
    private Integer withdrawMinAmount;
    @Column(name = "TARGET_WITHDRAW_MAX_AMOUNT", insertable = false, updatable = false)
    private Integer withdrawMaxAmount;

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
