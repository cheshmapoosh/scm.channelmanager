package ir.daneshrefah.scm.plugin.scm.service.person;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.data.service.person.PersonFindRequest;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.MethodNotSupportDataException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.model.customer.Customer;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.model.service.ExternalServiceProvider;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.plugin.api.service.CustomerService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-23
 */
@Service
public class PersonManagementService extends AbstractJavaService {

    private final ServiceService serviceService;
    private final CustomerService customerService;
    private final PersonService personService;

    public PersonManagementService(CustomerService customerService, ServiceService serviceService, PersonService personService,
                                   ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper) {
        super(producerTemplate, objectMapper);
        this.customerService = customerService;
        this.serviceService = serviceService;
        this.personService = personService;
    }

    public GeneralPerson findPersonInfo(PersonFindRequest request) {
        return null;
    }

    public List<GeneralPerson> findCIFPersonInfo(PersonFindRequest request) {
        return null;
    }

    public GeneralPerson saveOrUpdateLocalPersonInfoFromCIF(PersonFindRequest request) {
        return null;
    }







    public Customer findCustomerByProviderAndPersonId(String providerId, Long personId) {
        if (StringUtils.isEmpty(providerId)) {
            throw new MissingRequiredInputException("providerId");
        }
        if (null == personId) {
            throw new MissingRequiredInputException("personId");
        }
        ExternalServiceProvider provider = serviceService.findServiceProviderById(providerId);
        if (null == provider) {
            throw new InvalidInputException("providerId");
        }
        if (!provider.isCustomerProvided()) {
            throw new MethodNotSupportDataException("provider don't support customer.");
        }

        return customerService.findCustomerByPersonId(provider, personId);
    }

    public Customer findCustomerByProviderAndPersonProfileId(String providerId, String personProfileId) {
        if (StringUtils.isEmpty(providerId)) {
            throw new MissingRequiredInputException("providerId");
        }
        if (StringUtils.isEmpty(personProfileId)) {
            throw new MissingRequiredInputException("personProfileId");
        }
        ExternalServiceProvider provider = serviceService.findServiceProviderById(providerId);
        if (null == provider) {
            throw new InvalidInputException("providerId");
        }
        if (!provider.isCustomerProvided()) {
            throw new MethodNotSupportDataException("provider don't support customer.");
        }

        return customerService.findCustomerByPersonProfileId(provider, personProfileId);
    }

    public Customer synchronizeProviderCustomerInfoByPersonId(String providerId, Integer personId) {
        if (StringUtils.isEmpty(providerId)) {
            throw new MissingRequiredInputException("providerId");
        }
        if (null == personId) {
            throw new MissingRequiredInputException("personId");
        }
        ExternalServiceProvider provider = serviceService.findServiceProviderById(providerId);
        if (null == provider) {
            throw new InvalidInputException("providerId");
        }
        if (!provider.isCustomerProvided()) {
            throw new MethodNotSupportDataException("provider don't support customer.");
        }

        return customerService.synchronizeProviderCustomerInfoByPersonId(provider, personId);
    }

}
