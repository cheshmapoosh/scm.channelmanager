package ir.daneshrefah.scm.plugin.scm.service.person;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.exception.ResultNotFoundException;
import ir.daneshrefah.scm.common.exception.ValidationException;
import ir.daneshrefah.scm.common.model.customer.Customer;
import ir.daneshrefah.scm.common.model.service.ExternalServiceProvider;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.plugin.api.service.CustomerService;
import ir.daneshrefah.scm.common.data.service.person.PersonFindRequest;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.*;

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
        List<GeneralPerson> result = personService.findCIFPersonInfo(request);
        if (null == result) {
            throw new ResultNotFoundException("person", ERROR_CODE_VALIDATION_PERSON_NOT_FOUND,
                    String.format("person not found for personType: '%s', nationality: '%s', nationalId: '%s', subOrganizationId: '%s'.",
                            request.getPersonType(), request.getNationality(), request.getNationalId(), request.getSubOrganizationId()));
        }
        return result;
    }

    public GeneralPerson saveOrUpdateLocalPersonInfoFromCIF(PersonFindRequest request) {
        return null;
    }







    public Customer findCustomerByProviderAndPersonId(String providerId, Long personId) {
        if (StringUtils.isEmpty(providerId)) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_EXTERNAL_PROVIDER_IS_EMPTY, "provider id is empty.");
        }
        if (null == personId) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_PERSON_ID_IS_EMPTY, "person id is empty.");
        }
        ExternalServiceProvider provider = serviceService.findServiceProviderById(providerId);
        if (null == provider) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_EXTERNAL_PROVIDER_IS_INVALID, "provider id not found.");
        }
        if (!provider.isCustomerProvided()) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_EXTERNAL_PROVIDER_NOT_SUPPORT_CUSTOMER,
                    "provider don't support customer.");
        }

        return customerService.findCustomerByPersonId(provider, personId);
    }

    public Customer findCustomerByProviderAndPersonProfileId(String providerId, String personProfileId) {
        if (StringUtils.isEmpty(providerId)) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_EXTERNAL_PROVIDER_IS_EMPTY, "provider id is empty.");
        }
        if (StringUtils.isEmpty(personProfileId)) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_PERSON_PROFILE_ID_IS_EMPTY, "person profile id is empty.");
        }
        ExternalServiceProvider provider = serviceService.findServiceProviderById(providerId);
        if (null == provider) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_EXTERNAL_PROVIDER_IS_INVALID, "provider id not found.");
        }
        if (!provider.isCustomerProvided()) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_EXTERNAL_PROVIDER_NOT_SUPPORT_CUSTOMER,
                    "provider don't support customer.");
        }

        return customerService.findCustomerByPersonProfileId(provider, personProfileId);
    }

    public Customer synchronizeProviderCustomerInfoByPersonId(String providerId, Integer personId) {
        if (StringUtils.isEmpty(providerId)) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_EXTERNAL_PROVIDER_IS_EMPTY, "provider id is empty.");
        }
        if (null == personId) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_PERSON_ID_IS_EMPTY, "person id is empty.");
        }
        ExternalServiceProvider provider = serviceService.findServiceProviderById(providerId);
        if (null == provider) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_EXTERNAL_PROVIDER_IS_INVALID, "provider id not found.");
        }
        if (!provider.isCustomerProvided()) {
            throw new ValidationException(null, ERROR_CODE_VALIDATION_SERVICE_EXTERNAL_PROVIDER_NOT_SUPPORT_CUSTOMER,
                    "provider don't support customer.");
        }

        return customerService.synchronizeProviderCustomerInfoByPersonId(provider, personId);
    }

}
