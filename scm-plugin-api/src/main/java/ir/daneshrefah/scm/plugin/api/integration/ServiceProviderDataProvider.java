package ir.daneshrefah.scm.plugin.api.integration;

import ir.daneshrefah.scm.common.model.person.Customer;
import lombok.RequiredArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-14
 */
@RequiredArgsConstructor
public abstract class ServiceProviderDataProvider {

    public abstract Customer findCustomerByPersonProfile(String id);

}
