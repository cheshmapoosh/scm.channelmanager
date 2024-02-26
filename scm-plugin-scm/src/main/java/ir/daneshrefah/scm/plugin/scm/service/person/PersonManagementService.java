package ir.daneshrefah.scm.plugin.scm.service.person;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.data.service.person.PersonFindRequest;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.plugin.api.service.CustomerService;
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

}
