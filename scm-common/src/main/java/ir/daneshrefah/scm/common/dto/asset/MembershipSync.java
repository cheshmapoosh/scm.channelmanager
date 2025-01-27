package ir.daneshrefah.scm.common.dto.asset;

import ir.daneshrefah.scm.common.model.asset.AssetProvider;
import ir.daneshrefah.scm.common.model.asset.Membership;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MembershipSync {
    private Long accountNumber;
    private Membership localMembership;
    private GeneralPerson person;
    private AssetProvider assetProvider;
    private ExternalAccountResponseData remoteAccount;
    private MembershipSyncStatus syncStatus;

    @Getter
    public enum MembershipSyncStatus {
        CREATED,
        UPDATED,
        DELETED
    }

}
