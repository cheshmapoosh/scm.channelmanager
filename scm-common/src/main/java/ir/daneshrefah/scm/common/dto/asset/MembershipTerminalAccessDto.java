package ir.daneshrefah.scm.common.dto.asset;

import ir.daneshrefah.scm.common.AbstractAuditableModel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MembershipTerminalAccessDto extends AbstractAuditableModel<Long> {
    private Boolean active;
    private MembershipDto membership;
    private Boolean favorite;
    private String maxWithdrawalPerDay;
    private String maxPersWithdrawalPerDay;
    private String reason;
    private String userReason;
    private LocalDate fromDate;
    private LocalDate toDate;
}
