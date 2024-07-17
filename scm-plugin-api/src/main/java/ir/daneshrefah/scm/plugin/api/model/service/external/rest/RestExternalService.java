package ir.daneshrefah.scm.plugin.api.model.service.external.rest;

import ir.daneshrefah.scm.common.model.service.HttpContentType;
import ir.daneshrefah.scm.common.model.service.HttpMethod;
import ir.daneshrefah.scm.common.model.service.RestExternalServiceProvider;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractExternalService;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-14
 */
@Data
public class RestExternalService extends AbstractExternalService<RestExternalServiceProvider> {

    private String path;
    private HttpMethod httpMethod;
    private HttpContentType requestContentType;

}
