package ir.daneshrefah.scm.plugin.nab.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.person.GeneralLegalPerson;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.model.person.GeneralRealPerson;
import ir.daneshrefah.scm.common.model.person.Nationality;
import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.customer.Customer;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProviderDataProvider;
import ir.daneshrefah.scm.plugin.nab.repository.NabCustomerRepository;
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

    private final NabCustomerRepository customerRepository;
    private final ServiceProducerTemplate serviceProducerTemplate;

    @Override
    public Customer inquireCustomerByPerson(GeneralPerson person) {
        String nationalId = person instanceof GeneralRealPerson ?
                ((GeneralRealPerson) person).getNationalCode() : ((GeneralLegalPerson) person).getNationalId();
        String subOrganizationId = person instanceof GeneralRealPerson ?
                "0" : ((GeneralLegalPerson) person).getSubOrganizationId();
        return inquireCustomerByPerson(person.getPersonType(), person.getNationality(), nationalId, subOrganizationId);
    }

    @Override
    public Customer inquireCustomerByPerson(PersonType personType, Nationality nationality, String nationalId, String subOrganizationId) {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("customerType", "-1");
        payload.put("nationalId", nationalId);
        payload.put("subOrganizationId", subOrganizationId);
        Message message = serviceProducerTemplate.callService("SVC_NAB_FIND_CUSTOMER", payload);
        JsonNode customerNode = message.getPayload();
        Customer result = new Customer();
        result.setProviderId(provider.getId());
        result.setCustomerNo(customerNode.get(0).get("customerId").asText());
        return result;
    }

    @Override
    public Customer findCustomerByPersonId(Long personId) {
        return customerRepository.findAccountListByPersonId(personId);
    }

    @Override
    public Customer findCustomerByPersonProfileId(String personProfileId) {
        return null;
    }
}
