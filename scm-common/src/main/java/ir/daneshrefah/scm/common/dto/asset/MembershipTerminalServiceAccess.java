package ir.daneshrefah.scm.common.dto.asset;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.model.asset.MembershipTerminalAccess;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MembershipTerminalServiceAccess extends BaseModel<Integer> {
    private Integer id;
    private ChannelServiceAccess channelServiceAccess;
    private MembershipTerminalAccess membershipTerminalAccess;
    private Integer archiveNo;
    private BigDecimal maxWithdrawalPerTransaction;
}
