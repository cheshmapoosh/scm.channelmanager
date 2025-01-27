package ir.daneshrefah.scm.common.model.asset;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-25
 */
@Getter
@Setter
public class MembershipTerminalAccess extends BaseModel<Long> {

    private Boolean active;
    private Terminal terminal;
    private Membership membership;
    private Boolean favorite;
    private BigDecimal maxWithdrawalPerDay;
    private BigDecimal maxWithdrawalPerMonth;
    private LocalDate fromDate;
    private LocalDate toDate;
}
