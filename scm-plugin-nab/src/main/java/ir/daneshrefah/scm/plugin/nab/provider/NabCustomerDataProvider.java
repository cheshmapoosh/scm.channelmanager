package ir.daneshrefah.scm.plugin.nab.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.exception.MethodNotSupportDataException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.customer.AccountAsset;
import ir.daneshrefah.scm.common.model.customer.Asset;
import ir.daneshrefah.scm.common.model.customer.Customer;
import ir.daneshrefah.scm.common.model.customer.PersonProfile;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.person.*;
import ir.daneshrefah.scm.common.model.service.ExternalServiceProvider;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProviderDataProvider;
import ir.daneshrefah.scm.plugin.nab.repository.NabCustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

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

    private final NabCustomerRepository customerRepository;
    private final ServiceProducerTemplate serviceProducerTemplate;

    @Override
    public Customer inquireRemoteCustomerByPerson(GeneralPerson person) {
        String nationalId = person instanceof GeneralRealPerson ?
                ((GeneralRealPerson) person).getNationalCode() : ((GeneralLegalPerson) person).getNationalId();
        String subOrganizationId = person instanceof GeneralRealPerson ?
                "0" : ((GeneralLegalPerson) person).getSubOrganizationId();
        return inquireRemoteCustomerByPerson(person.getPersonType(), person.getNationality(), nationalId, subOrganizationId);
    }

    @Override
    public Customer inquireRemoteCustomerByPerson(PersonType personType, Nationality nationality, String nationalId, String subOrganizationId) {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("customerType", "-1");
        payload.put("nationalId", nationalId);
        payload.put("subOrganizationId", subOrganizationId);
        Message message = serviceProducerTemplate.callServiceWithException("SVC_NAB_FIND_CUSTOMER", payload);
        JsonNode customerNode = message.getPayload();
        Customer result = new Customer();
        result.setProviderId(provider.getId());
        result.setCustomerNo(customerNode.get(0).get("customerId").asText());
        return result;
    }

    @Override
    public Customer inquireRemoteCustomerAssetList(PersonType personType, Nationality nationality, String nationalId, String subOrganizationId) {
        //TODO ASSET
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("customerType", "-1");
        payload.put("nationalId", nationalId);
        payload.put("subOrganizationId", subOrganizationId);
        Message message = serviceProducerTemplate.callServiceWithException("SVC_NAB_FIND_CUSTOMER_ACCOUNTS", payload);
        JsonNode customerNode = message.getPayload();
        Customer result = new Customer();
        result.setProviderId(provider.getId());
        result.setCustomerNo(customerNode.get(0).get("customerId").asText());
        return result;
    }

    @Override
    public Customer findLocalCustomerByPersonId(Long personId) {
        return customerRepository.findAccountListByPersonId(personId);
    }

    @Override
    public Customer findLocalCustomerByPersonProfileId(String personProfileId) {
        return null;
    }

    @Override
    public <T extends Asset> List<T> findLocalCustomerAssetListByPersonId(PersonProfile.PersonId personId, Class<T> clazz) {
        //TODO ASSET
        return null;
    }

    @Override
    public Customer synchronizeCustomerInfo(ExternalServiceProvider provider, GeneralPerson person) {
        if (null == person) {
            throw new MissingRequiredInputException("person");
        }
        if (null == provider) {
            throw new MissingRequiredInputException("provider");
        }
        if (!provider.isCustomerProvided()) {
            throw new MethodNotSupportDataException(provider.getCode());
        }
        Customer remoteCustomer = inquireRemoteCustomerByPerson(person);
        if (null == remoteCustomer) {
            throw new NoMatchRecordFoundException(provider.getCode(), person.getUsername());
        }
        Customer customer = findLocalCustomerByPersonId(person.getId().longValue());
        if (null == customer) {
            customer = new Customer();
            customer.setCustomerNo(remoteCustomer.getCustomerNo());
            customer.setProviderId(provider.getId());
            customerRepository.saveCustomer(customer);
        }
        //TODO ASSET
//        List<AccountAsset>
        for (Iterator<? extends Asset> iterator = customer.getAssets().iterator(); iterator.hasNext(); ) {
            Asset remoteAsset = iterator.next();
            AccountAsset asset = AccountAsset.builder()
                    .build();

//            CUSTOMER
//            ACCOUNT_TYPE
//            ACCOUNT
//            CUSTOMERACCOUNT
        }
        return null;
    }
}
