package ir.daneshrefah.scm.core.model.person;

import ir.daneshrefah.scm.common.BaseModel;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-13
 */
public class PersonProfile extends BaseModel<String> {

    private List<Customer> customers;
    private List<ServiceAccess> services;

    public boolean hasServiceAccess(String serviceCode, Object asset) {
        if (null == services || services.isEmpty()) {
            return false;
        }
        return services.contains(serviceCode);
    }

    public Asset findAsset(String providerId, Object assetValue) {
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
