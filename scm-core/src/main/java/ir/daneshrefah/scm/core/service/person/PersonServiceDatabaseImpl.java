package ir.daneshrefah.scm.core.service.person;

import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.data.mapper.PersonMapper;
import ir.daneshrefah.scm.common.data.repository.PersonRepository;
import ir.daneshrefah.scm.common.data.type.Nationality;
import ir.daneshrefah.scm.common.data.type.PersonType;
import ir.daneshrefah.scm.plugin.api.integration.ErrorHandlerService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.common.data.model.person.GeneralPerson;
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
    public GeneralPerson findPersonByPersonInfo(PersonType personType, Nationality nationality, String nationalId, String subOrganizationId) {
        GeneralPersonEntity entity = null;
        switch (personType) {
            case INDIVIDUAL_CUSTOMER:
                entity = personRepository.findIndividualPersonByNationalCode(nationalId);
                break;
            case EMPLOYEE:
                entity = personRepository.findEmployeePersonByNationalCode(nationalId);
                break;
            case CORPORATE_CUSTOMER:
                entity = personRepository.findCorporatePersonByNationalCode(nationalId, subOrganizationId);
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
