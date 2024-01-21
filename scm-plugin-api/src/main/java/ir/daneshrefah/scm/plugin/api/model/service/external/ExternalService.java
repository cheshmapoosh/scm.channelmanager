package ir.daneshrefah.scm.plugin.api.model.service.external;

import ir.daneshrefah.scm.common.model.service.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
public class ExternalService extends Service {

    private ExternalServiceProvider serviceProvider;

    public ExternalServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    public void setServiceProvider(ExternalServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

}
