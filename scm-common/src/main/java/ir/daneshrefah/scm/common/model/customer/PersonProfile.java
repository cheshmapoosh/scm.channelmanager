package ir.daneshrefah.scm.common.model.customer;

import ir.daneshrefah.scm.common.model.asset.Customer;
import ir.daneshrefah.scm.common.model.asset.MembershipTerminalAccess;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-13
 */
public class PersonProfile implements Serializable {

    @Getter
    private final PersonId personId;
    private List<MembershipTerminalAccess> memberships;
//    private final Map<String, Customer> customers = new HashMap<>();

    @Getter
    @Setter
    private List<ServiceAccess> serviceAccesses;

    public PersonProfile(@NonNull String username, @NonNull Long id) {
        this.personId = new PersonId(username, id);
    }

    public void loadMembership(List<MembershipTerminalAccess> memberships) {
        this.memberships = memberships;
    }

    public List<MembershipTerminalAccess> getMemberships() {
        return this.memberships;
    }

    public boolean hasMembership(String providerId) {
        if (StringUtils.isEmpty(providerId) || null == memberships || memberships.size() < 1) {
            return false;
        }
        return memberships.stream().anyMatch(m -> providerId.equals(m.getMembership().getCustomerAccount().getCustomer().getProvider().getId()));
    }

    public Customer getCustomer(String providerId) {
        if (StringUtils.isEmpty(providerId) || null == memberships || memberships.size() < 1) {
            return null;
        }
        Optional<MembershipTerminalAccess> mta = memberships.stream().filter(m -> providerId.equals(m.getMembership().getCustomerAccount().getCustomer().getProvider().getId())).findFirst();
        return mta.isPresent() ? mta.get().getMembership().getCustomerAccount().getCustomer() : null;
    }

    public boolean isMembershipLoaded() {
        return null != memberships;
    }

    /*public boolean isCustomerAssetLoaded(String providerId) {
        return null != customers && null != customers.get(providerId) && null != customers.get(providerId).getAssets();
    }

    public void addCustomer(String providerId, Customer customer) {
        customers.put(providerId, customer);
    }*/

    public boolean hasServiceAccess(String terminalCode, String serviceCode, Object asset) {
        if (null == serviceAccesses || serviceAccesses.isEmpty()) {
            return false;
        }
        return serviceAccesses.stream()
                .anyMatch(serviceAccess -> {
                    return (serviceAccess.getService().getCode().equals(serviceCode) &&
                            (null == serviceAccess.getAssetId() ||
                                    serviceAccess.getAssetId().equals(asset)));
                });
    }

    public MembershipTerminalAccess findAsset(String providerId, String assetValue, AssetType assetType) {
        if (StringUtils.isEmpty(providerId) || StringUtils.isEmpty(assetValue) || null == memberships || memberships.size() < 1) {
            return null;
        }
        return memberships.stream().filter(m ->
                        providerId.equals(m.getMembership().getCustomerAccount().getCustomer().getProvider().getId()) &&
                                (null == assetType || assetType.equals(m.getMembership().getAssetType())) &&
                                (m.getMembership().getCustomerAccount().getAccount().getAccountNo().equalsIgnoreCase(assetValue))

                )
                .findFirst().orElse(null);
    }

    public boolean hasAssetAccess(String providerId, String assetValue, AssetType assetType) {
        return null != findAsset(providerId, assetValue, assetType);
    }

    /**
     * personProfileId ref to USER.USERNAME
     * personId ref to USER.USER_ID
     */
    public record PersonId(String username, Long id) implements Serializable {
    }

}
