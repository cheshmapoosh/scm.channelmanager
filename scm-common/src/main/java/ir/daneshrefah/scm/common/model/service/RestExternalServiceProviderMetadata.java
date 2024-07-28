package ir.daneshrefah.scm.common.model.service;

import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-12
 */
@Data
public class RestExternalServiceProviderMetadata extends AbstractExternalServiceProviderMetadata {

    private HttpMethod defaultHttpMethod;
    private HttpContentType defaultRequestContentType;

}
