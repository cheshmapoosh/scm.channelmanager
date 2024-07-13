package ir.daneshrefah.scm.plugin.api.model.service.external;

import ir.daneshrefah.scm.common.model.service.AbstractExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
public class ExternalService extends Service {

    private AbstractExternalServiceProvider serviceProvider;

    public AbstractExternalServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    public void setServiceProvider(AbstractExternalServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

}
