package ir.daneshrefah.scm.uaa.service.person;

import ir.daneshrefah.scm.common.data.entity.person.GeneralLegalPersonEntity;
import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.data.entity.person.GeneralRealPersonEntity;
import ir.daneshrefah.scm.common.data.mapper.PersonMapper;
import ir.daneshrefah.scm.common.data.repository.PersonRepository;
import ir.daneshrefah.scm.common.data.service.person.AbstractPersonServiceDatabaseImpl;
import ir.daneshrefah.scm.common.data.service.person.PersonFindRequest;
import ir.daneshrefah.scm.common.exception.InputAlreadyExistException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.exception.TooManyRecordFoundException;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.utils.string.StringUtils;
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
public class PersonServiceDatabaseImpl extends AbstractPersonServiceDatabaseImpl implements UPersonService {

    private final CIFService cifService;

    public PersonServiceDatabaseImpl(PersonRepository personRepository, CIFService cifService) {
        super(personRepository);
        this.cifService = cifService;
    }

    @Override
    public List<GeneralPerson> findCIFPersonInfo(PersonFindRequest request) {
        return cifService.findPersonInfo(request);
    }

    @Override
    public GeneralPerson addPersonInfoFromCIF(PersonFindRequest request) {
        if (null == request) {
            throw new MissingRequiredInputException("request body");
        }
        if (null == request.getNationalId()) {
            throw new MissingRequiredInputException("nationalId");
        }
        boolean isPersonExist = checkPersonExist(request);
        if (isPersonExist) {
            throw new InputAlreadyExistException("person");
        }
        List<GeneralPerson> person = findCIFPersonInfo(request);
        if (null == person || person.size() < 1) {
            throw new NoMatchRecordFoundException("cif person");
        }
        if (person.size() > 1) {
            throw new TooManyRecordFoundException("cif person", person.size());
        }
        GeneralPersonEntity personEntity = PersonMapper.INSTANCE.toPersonEntity(person.get(0));
        personEntity.setUsername(extractUsername(personEntity));
        personEntity.setActive(true);
        personEntity.setArchiveNo(12);
//        personEntity.setCreator
        personEntity = personRepository.save(personEntity);
        return PersonMapper.INSTANCE.toPerson(personEntity);
    }

    private String extractUsername(GeneralPersonEntity personEntity) {
        if (null == personEntity) {
            return null;
        }
        if (personEntity instanceof GeneralRealPersonEntity) {
            return ((GeneralRealPersonEntity) personEntity).getNationalCode();
        } else {
            GeneralLegalPersonEntity legalPersonEntity = (GeneralLegalPersonEntity) personEntity;
            String subOrganizationCode = null != legalPersonEntity.getSubOrganizationId() ?
                    legalPersonEntity.getSubOrganizationId() : StringUtils.EMPTY;
            return legalPersonEntity.getNationalId() + subOrganizationCode;
        }
    }

    @Override
    public GeneralPerson updatePersonInfoFromCIF(PersonFindRequest request) {
        return null;
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
