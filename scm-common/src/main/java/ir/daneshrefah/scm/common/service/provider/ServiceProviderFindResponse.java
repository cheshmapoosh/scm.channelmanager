package ir.daneshrefah.scm.common.service.provider;

import ir.daneshrefah.scm.common.model.asset.AssetProvider;
import ir.daneshrefah.scm.common.model.service.ServiceProviderProtocol;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ServiceProviderFindResponse {
    private String code;
    private String title;
    private String providerClassName;
    private ServiceProviderProtocol protocol;
    private AssetProvider assetProvider;
}
