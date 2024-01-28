package ir.daneshrefah.scm.core.service.person;

import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.data.mapper.PersonMapper;
import ir.daneshrefah.scm.common.data.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.data.repository.PersonRepository;
import ir.daneshrefah.scm.plugin.api.integration.ErrorHandlerService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.person.PersonInfoRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;


/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-24
 */
@Service
public class PersonServiceDatabaseImpl extends AbstractPersonService {

    private final PersonRepository personRepository;

    public PersonServiceDatabaseImpl(PersonRepository personRepository, ServiceProducerTemplate serviceProducerTemplate,
                                     ErrorHandlerService errorHandlerService) {
        super(serviceProducerTemplate, errorHandlerService);
        this.personRepository = personRepository;
    }

    @Override
    public GeneralPerson findPersonInfo(PersonInfoRequest request) {
        GeneralPersonEntity entity = null;
        switch (request.getPersonType()) {
            case INDIVIDUAL_CUSTOMER:
                entity = personRepository.findIndividualPersonByNationalCode(request.getNationalId());
                break;
            case EMPLOYEE:
                entity = personRepository.findEmployeePersonByNationalCode(request.getNationalId());
                break;
            case CORPORATE_CUSTOMER:
                entity = personRepository.findCorporatePersonByNationalCode(request.getNationalId(), request.getSubOrganizationId());
                break;
        }
        if (null == entity) {
            return null;
        }
        return PersonMapper.INSTANCE.toPerson(entity);
    }

    @Override
    public GeneralPerson findPersonByPersonId(Long id) {
        if (null == id) {
            return null;
        }
        Optional<GeneralPersonEntity> personEntity = personRepository.findById(id.intValue());
        if (personEntity.isEmpty()) {
            return null;
        }
        return PersonMapper.INSTANCE.toPerson(personEntity.get());
    }

    @Override
    public GeneralPerson findPersonByPersonProfileId(String id) {
        return null;
    }

}
