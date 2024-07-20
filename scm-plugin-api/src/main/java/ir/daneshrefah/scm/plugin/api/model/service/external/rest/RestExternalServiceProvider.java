package ir.daneshrefah.scm.plugin.api.model.service.external.rest;

import ir.daneshrefah.scm.common.model.service.AbstractExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.RestExternalServiceProviderMetadata;
import lombok.Data;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-12
 */
@Data
public class RestExternalServiceProvider extends AbstractExternalServiceProvider {

    private RestExternalServiceProviderMetadata metadata;
    private List<RestResponseCondition> responseConditions;

}
