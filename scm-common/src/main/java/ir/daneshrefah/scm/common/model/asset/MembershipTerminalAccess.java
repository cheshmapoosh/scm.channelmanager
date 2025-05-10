package ir.daneshrefah.scm.common.model.asset;

import ir.daneshrefah.scm.common.AuditableModel;
import ir.daneshrefah.scm.common.model.gateway.CmChannel;
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
public class MembershipTerminalAccess extends AuditableModel<Long> {

    private Boolean active;
    private CmChannel channel;
    private Membership membership;
    private Boolean favorite;
    private BigDecimal maxWithdrawalPerDay;
    private BigDecimal maxPersWithdrawalPerDay;
    private String reason;
    private String userReason;
    private LocalDate fromDate;
    private LocalDate toDate;
}
