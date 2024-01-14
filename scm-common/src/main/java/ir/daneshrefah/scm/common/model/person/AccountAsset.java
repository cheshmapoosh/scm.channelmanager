package ir.daneshrefah.scm.common.model.person;

import lombok.Builder;
import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-13
 */
// REF.CUSTOMERACCOUNT
// REF.MEMBERSHIP
// REF.MEMBERSHIP_CHANNEL_ACCESS
// REF.MEMBERSHIP_CHANNEL_ACCESS_AUTHENTICATION_METHOD
// REF.MEMBERSHIP_CHANNEL_SERVICE_ACCESS
@Getter
@Builder
public class AccountAsset extends Asset<String> {

    private Account account;

    @Override
    public String getValue() {
        return account.getAccountNo();
    }

    @Override
    public AssetType getType() {
        return AssetType.ACCOUNT;
    }

}
