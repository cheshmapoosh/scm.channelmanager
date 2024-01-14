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

    /**
     * ref to USER.USERNAME
     */
    @Getter
    private String personProfileId;
    /**
     * ref to USER.USER_ID
     */
    private Long personId;
    private Map<String, Customer> customers;

    @Getter
    @Setter
    private List<ServiceAccess> serviceAccesses;

    public PersonProfile(@NonNull String personProfileId) {
        this.personProfileId = personProfileId;
        customers = new HashMap<>();
    }

    public Customer getCustomer(String providerId) {
        return customers.get(providerId);
    }

    public void addCustomer(String providerId, Customer customer) {
        customers.put(providerId, customer);
    }

    public boolean hasServiceAccess(String serviceCode, Object asset) {
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

}
