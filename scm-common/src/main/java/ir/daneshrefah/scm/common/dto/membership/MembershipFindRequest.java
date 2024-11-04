package ir.daneshrefah.scm.common.dto.membership;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MembershipFindRequest extends MembershipLocalFindRequest {
    String assetProviderId;
}
