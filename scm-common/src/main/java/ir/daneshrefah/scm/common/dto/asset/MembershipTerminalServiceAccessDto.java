package ir.daneshrefah.scm.common.dto.asset;

import ir.daneshrefah.scm.common.dto.spec.ResponseData;
import ir.daneshrefah.scm.common.model.gateway.Service;
import lombok.Data;

@Data
public class MembershipTerminalServiceAccessDto implements ResponseData {
    private Integer id;
    private String maxWithdrawalPerTransaction;
    private Service ebService;
}
