package ir.daneshrefah.scm.common.dto.asset;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.model.asset.CustomerAccount;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MembershipDto extends BaseModel<Long> {
    private String nickname;
    private CustomerAccount customerAccount;
    private Boolean defaultAccount;
    private Integer archiveNumber;
    private Boolean close;
}
