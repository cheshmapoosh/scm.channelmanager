package ir.daneshrefah.scm.common.model.customer;

import ir.daneshrefah.scm.common.model.asset.Customer;
import ir.daneshrefah.scm.common.model.asset.MembershipTerminalAccess;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ir.daneshrefah.scm.common.constant.SecurityConstants.USERNAME_ANONYMOUS;

/**
 *
 * User Profile hold the data of effective user. it means that in delegation mode it holds the data of delegated user.
 * but in normal mode it holds the data of logged-in user.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-13
 */
@Getter
public class UserProfile implements Serializable {

    /**
     * this property in delegation mode refer to delegated user and in normal mode hold the logged-in nickname
     * */
    private final String nickname;
    private String personUsername;
    private Long personId;
    private List<MembershipTerminalAccess> memberships;

    @Setter
    private List<ServiceAccess> serviceAccesses;

    public UserProfile(@NonNull String nickname, String personUsername, Long personId) {
        this.nickname = nickname;
        this.personUsername = personUsername;
        this.personId = personId;
    }

    public UserProfile(@NonNull String nickname) {
        this.nickname = nickname;
    }

    public UserProfile() {
        this.nickname = USERNAME_ANONYMOUS;
    }

    public void loadMembership(List<MembershipTerminalAccess> memberships) {
        this.memberships = memberships;
    }

    public boolean hasMembership(Integer assetProviderId) {
        if (Objects.isNull(assetProviderId) || Objects.isNull(memberships) || memberships.size() < 1) {
            return false;
        }
        return memberships.stream().anyMatch(m -> assetProviderId.equals(m.getMembership().getCustomerAccount().getAccount().getAssetProvider().getId()));
    }

    public Customer getCustomer(Integer assetProviderId) {
        if (Objects.isNull(assetProviderId) || null == memberships || memberships.size() < 1) {
            return null;
        }
        Optional<MembershipTerminalAccess> mta = memberships.stream().filter(m ->
                assetProviderId.equals(m.getMembership().getCustomerAccount().getAccount().getAssetProvider().getId())).findFirst();
        return mta.isPresent() ? mta.get().getMembership().getCustomerAccount().getCustomer() : null;
    }

    public boolean isMembershipLoaded() {
        return null != memberships;
    }

    public boolean isPersonInfoLoaded() {
        return StringUtils.isNotEmpty(personUsername);
    }

    public void loadPersonInfo(@NonNull String personUsername, @NonNull Long personId) {
        this.personUsername = personUsername;
        this.personId = personId;
    }

    public boolean hasServiceAccess(String terminalCode, String serviceCode, Object asset) {
        if (null == serviceAccesses || serviceAccesses.isEmpty()) {
            return false;
        }
        return serviceAccesses.stream()
                .anyMatch(serviceAccess -> (
                        serviceAccess.getService().getCode().equals(serviceCode) &&
                        (Objects.isNull(serviceAccess.getTerminal()) || StringUtils.isEmpty(terminalCode) || serviceAccess.getTerminal().getCode().equals(terminalCode)) &&
                        (null == serviceAccess.getAssetId() || serviceAccess.getAssetId().equals(asset))
                ));
    }

    public MembershipTerminalAccess findAsset(Integer assetProviderId, String assetValue, AssetType assetType) {
        if (Objects.isNull(assetProviderId) || StringUtils.isEmpty(assetValue) || null == memberships || memberships.size() < 1) {
            return null;
        }
        return memberships.stream().filter(m ->
                        assetProviderId.equals(m.getMembership().getCustomerAccount().getAccount().getAssetProvider().getId()) &&
                                (null == assetType || assetType.equals(m.getMembership().getAssetType())) &&
                                (m.getMembership().getCustomerAccount().getAccount().getAccountNo().equalsIgnoreCase(assetValue))

                )
                .findFirst().orElse(null);
    }

    public boolean hasAssetAccess(Integer assetProviderId, String assetValue, AssetType assetType) {
        return null != findAsset(assetProviderId, assetValue, assetType);
    }

}
