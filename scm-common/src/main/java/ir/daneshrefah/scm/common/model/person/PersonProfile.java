package ir.daneshrefah.scm.common.model.person;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-13
 */
public class PersonProfile implements Serializable {

    @Getter
    private PersonId personId;
    private Map<String, Customer> customers = new HashMap<>();

    @Getter
    @Setter
    private List<ServiceAccess> serviceAccesses;

    public PersonProfile(@NonNull String personProfileId, @NonNull Long personId) {
        this.personId = new PersonId(personProfileId, personId);
    }

    public Customer getCustomer(String providerId) {
        return customers.get(providerId);
    }

    public boolean isCustomerLoaded(String providerId) {
        return null != customers && customers.containsKey(providerId);
    }

    public boolean isCustomerAssetLoaded(String providerId) {
        return null != customers && null != customers.get(providerId) && null != customers.get(providerId).getAssets();
    }

    public void addCustomer(String providerId, Customer customer) {
        customers.put(providerId, customer);
    }

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

    public Asset findAsset(String providerId, Object assetValue) {
        if (null == customers) {
            return null;
        }
        Customer customer = customers.get(providerId);
        if (null == customer) {
            return null;
        }
        for (Asset asset : customer.getAssets()) {
            if (asset.getValue().equals(assetValue)) {
                return asset;
            }
        }
        return null;
    }

    public boolean hasAssetAccess(String providerId, Object assetValue) {
        return null != findAsset(providerId, assetValue);
    }

    /**
     * personProfileId ref to USER.USERNAME
     * personId ref to USER.USER_ID
     */
    public record PersonId(String personProfileId, Long personId) implements Serializable {}

}
