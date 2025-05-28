package ir.daneshrefah.scm.common.model.service;

import ir.daneshrefah.scm.common.AbstractStringAuditableModel;
import ir.daneshrefah.scm.common.model.asset.AssetProvider;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-26
 */
@Getter
@Setter
public abstract class AbstractAuditableExternalServiceProvider extends AbstractStringAuditableModel<String> {

    private String code;
    private String title;
    private String providerClassName;
    private ServiceProviderProtocol protocol;
    private ServiceProviderStatus status;
    private AbstractExternalServiceProviderMetadata metadata;
    private AssetProvider assetProvider;

}
