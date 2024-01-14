package ir.daneshrefah.scm.plugin.nab.provider;

import ir.daneshrefah.scm.plugin.api.integration.ServiceProviderDataProvider;
import ir.daneshrefah.scm.common.model.person.Customer;
import ir.daneshrefah.scm.plugin.nab.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-14
 */
@RequiredArgsConstructor
@Service
public class NabCustomerDataProvider extends ServiceProviderDataProvider {

    private final CustomerRepository customerRepository;

    @Override
    public Customer findCustomerByPersonProfile(String id) {
        return customerRepository.findAccountListByPersonId(id);
    }
}
