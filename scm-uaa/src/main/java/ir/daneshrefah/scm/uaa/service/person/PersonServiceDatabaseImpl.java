package ir.daneshrefah.scm.uaa.service.person;

import ir.daneshrefah.scm.common.data.repository.PersonRepository;
import ir.daneshrefah.scm.common.data.service.person.AbstractPersonServiceDatabaseImpl;
import ir.daneshrefah.scm.common.data.service.person.PersonFindRequest;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-24
 */
@Service
public class PersonServiceDatabaseImpl extends AbstractPersonServiceDatabaseImpl {

    private final CIFService cifService;

    public PersonServiceDatabaseImpl(PersonRepository personRepository, CIFService cifService) {
        super(personRepository);
        this.cifService = cifService;
    }

    @Override
    public List<GeneralPerson> findCIFPersonInfo(PersonFindRequest request) {
        return cifService.findPersonInfo(request);
    }

//    @Override
//    public GeneralPerson findPersonInfo(PersonFindRequest request) {
//        GeneralPersonEntity entity = null;
//        switch (request.getPersonType()) {
//            case INDIVIDUAL_CUSTOMER:
//                entity = personRepository.findIndividualPersonByNationalCode(request.getNationalId());
//                break;
//            case EMPLOYEE:
//                entity = personRepository.findEmployeePersonByNationalCode(request.getNationalId());
//                break;
//            case CORPORATE_CUSTOMER:
//                entity = personRepository.findCorporatePersonByNationalCode(request.getNationalId(), request.getSubOrganizationId());
//                break;
//        }
//        if (null == entity) {
//            return null;
//        }
//        return PersonMapper.INSTANCE.toPerson(entity);
//    }
//
//    @Override
//    public GeneralPerson findPersonByPersonId(Long id) {
//        if (null == id) {
//            return null;
//        }
//        Optional<GeneralPersonEntity> personEntity = personRepository.findById(id.intValue());
//        if (personEntity.isEmpty()) {
//            return null;
//        }
//        return PersonMapper.INSTANCE.toPerson(personEntity.get());
//    }
//
//    @Override
//    public GeneralPerson findPersonByPersonProfileId(String id) {
//        return null;
//    }
//
//    @Override
//    public GeneralPerson updatePerson(GeneralPerson person) {
//        GeneralPersonEntity entity = PersonMapper.INSTANCE.toPersonEntity(person);
//        return PersonMapper.INSTANCE.toPerson(entity);
//    }
//
//    @Override
//    public GeneralPerson savePerson(GeneralPerson person) {
//        GeneralPersonEntity entity = PersonMapper.INSTANCE.toPersonEntity(person);
//        return PersonMapper.INSTANCE.toPerson(entity);
//    }

}
