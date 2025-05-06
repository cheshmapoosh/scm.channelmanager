package ir.daneshrefah.scm.common.dto.asset;

import ir.daneshrefah.scm.common.dto.spec.ResponseData;
import lombok.Data;

@Data
public class MembershipTerminalServiceAccessDto implements ResponseData {
    private String maxWithdrawalPerTransaction;
    private EbService ebService;
}
