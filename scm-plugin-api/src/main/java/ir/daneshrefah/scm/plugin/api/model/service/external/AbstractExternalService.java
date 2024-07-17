package ir.daneshrefah.scm.plugin.api.model.service.external;

import ir.daneshrefah.scm.common.model.service.AbstractExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.ExternalServiceRequestBodyType;
import ir.daneshrefah.scm.common.model.service.Service;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
@Data
public abstract class AbstractExternalService<T extends AbstractExternalServiceProvider> extends Service {

    private T serviceProvider;
    private ExternalServiceRequestBodyType requestBodyType;

}
