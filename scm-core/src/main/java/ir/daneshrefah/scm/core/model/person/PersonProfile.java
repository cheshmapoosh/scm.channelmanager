package ir.daneshrefah.scm.core.model.person;

import ir.daneshrefah.scm.common.BaseModel;
import lombok.Builder;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-13
 */
@Builder
public class PersonProfile extends BaseModel<String> {

    private List<Customer> customers;
    private List<ServiceAccess> serviceAccesses;

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
        for (Customer customer : customers) {
            if (customer.getProvider().getId() == providerId) {
                for (Asset asset : customer.getAssets()) {
                    if (asset.getValue().equals(assetValue)) {
                        return asset;
                    }
                }
            }
        }
        return null;
    }

    public boolean hasAssetAccess(String providerId, Object assetValue) {
        return null != findAsset(providerId, assetValue);
    }

}
