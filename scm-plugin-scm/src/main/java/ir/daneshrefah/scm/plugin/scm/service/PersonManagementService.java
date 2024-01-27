package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.data.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.data.type.Nationality;
import ir.daneshrefah.scm.common.data.type.PersonType;
import ir.daneshrefah.scm.common.exception.ValidationException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.person.Customer;
import ir.daneshrefah.scm.common.model.service.ExternalServiceProvider;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.plugin.api.service.CustomerService;
import ir.daneshrefah.scm.plugin.api.service.PersonService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.stereotype.Service;

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

    public GeneralPerson defineOrUpdatePersonInfo(Message message) {
        String personTypeCode = message.getPayloadValue("personType");
        if (StringUtils.isEmpty(personTypeCode)) {
            throw new ValidationException("personType", ERROR_CODE_VALIDATION_PERSON_TYPE_IS_EMPTY, "personType is empty.");
        }
        PersonType personType = PersonType.findByCode(personTypeCode);
        String nationalityCode = message.getPayloadValue("nationality");
//        if (StringUtils.isEmpty(nationalityCode)) {
//            throw new ValidationException("nationality", ERROR_CODE_VALIDATION_PERSON_NATIONALITY_IS_EMPTY, "nationality is empty.");
//        }
        Nationality nationality = Nationality.findByCode(nationalityCode);
        String nationalId = message.getPayloadValue("nationalId");
        String subOrganizationId = message.getPayloadValue("subOrganizationId");
        return personService.defineOrUpdatePersonInfo(personType, nationality, nationalId, subOrganizationId);
    }

    public Customer synchronizeProviderCustomerInfoByPersonId(String providerId, Long personId) {
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
