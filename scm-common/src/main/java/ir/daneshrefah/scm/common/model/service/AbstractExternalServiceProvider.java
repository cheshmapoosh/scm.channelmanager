package ir.daneshrefah.scm.common.model.service;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.model.asset.AssetProvider;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.common.model.service.parameter.Response;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-26
 */
@Getter
@Setter
public abstract class AbstractExternalServiceProvider extends BaseModel<String> {

    private String code;
    private String title;
    private String providerClassName;
    private ServiceProviderProtocol protocol;
    private ServiceProviderStatus status;
//    private AbstractExternalServiceProviderMetadata metadata;
    private AssetProvider assetProvider;
    private List<Parameter> requestHeaders;
    private List<Parameter> requestBody;
    private List<Parameter> responseHeaders;
    private List<Response> responseConditions;

    public abstract AbstractExternalServiceProviderMetadata getMetadata();

}
