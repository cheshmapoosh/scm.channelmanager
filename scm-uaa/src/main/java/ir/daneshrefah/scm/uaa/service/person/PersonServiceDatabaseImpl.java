package ir.daneshrefah.scm.uaa.service.person;

import ir.daneshrefah.scm.common.data.entity.person.GeneralLegalPersonEntity;
import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.data.entity.person.GeneralRealPersonEntity;
import ir.daneshrefah.scm.common.data.mapper.PersonMapper;
import ir.daneshrefah.scm.common.data.repository.PersonRepository;
import ir.daneshrefah.scm.common.data.service.person.AbstractPersonServiceDatabaseImpl;
import ir.daneshrefah.scm.common.data.service.person.PersonFindRequest;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.exception.TooManyRecordFoundException;
import ir.daneshrefah.scm.common.model.person.*;
import ir.daneshrefah.scm.common.service.terminal.TerminalService;
import ir.daneshrefah.scm.uaa.domain.role.Role;
import ir.daneshrefah.scm.uaa.mapper.RoleMapper;
import ir.daneshrefah.scm.uaa.repository.authentication.RoleEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.RoleRepository;
import ir.daneshrefah.scm.utils.string.ArchiveUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


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
    private final RoleRepository roleRepository;

    public PersonServiceDatabaseImpl(PersonRepository personRepository, TerminalService terminalService, RoleRepository roleRepository,
                                     CIFService cifService) {
        super(terminalService, personRepository);
        this.roleRepository = roleRepository;
        this.cifService = cifService;
    }

    @Override
    public List<GeneralPerson> findCIFPersonInfo(PersonFindRequest request) {
        return cifService.findPersonInfo(request);
    }

    private String extractUsername(GeneralPersonEntity personEntity) {
        if (null == personEntity) {
            return null;
        }
        if (personEntity instanceof GeneralRealPersonEntity) {
            return ((GeneralRealPersonEntity) personEntity).getNationalCode();
        } else {
            GeneralLegalPersonEntity legalPersonEntity = (GeneralLegalPersonEntity) personEntity;
            String subOrganizationCode = null != legalPersonEntity.getSubOrganizationId() ? legalPersonEntity.getSubOrganizationId() : StringUtils.EMPTY;
            return legalPersonEntity.getNationalId() + subOrganizationCode;
        }
    }

    @Override
    public GeneralPerson syncPersonInfoFromCIF(PersonFindRequest request) {
        ValidationUtils.checkNull(request, () -> new MissingRequiredInputException("request body"));
        ValidationUtils.checkBlankString(request.getNationalId(), () -> new MissingRequiredInputException("nationalId"));
        List<GeneralPerson> cifPersonInfo = findCIFPersonInfo(request);
        ValidationUtils.checkNullOrEmptyList(cifPersonInfo, () -> new NoMatchRecordFoundException("cif person"));
        return saveFoundCif(cifPersonInfo);
    }

    @Override
    public GeneralPerson syncPersonInfoFromCIF(String personId) {
        ValidationUtils.checkBlankString(personId, () -> new MissingRequiredInputException("personId"));
        GeneralPerson localPersonInfo = findPersonByPersonId(Integer.parseInt(personId));
        ValidationUtils.checkNull(localPersonInfo, () -> new NoMatchRecordFoundException("local person not found"));
        PersonFindRequest request = createFindRequestFromLocalPerson(localPersonInfo);
        return syncPersonInfoFromCIF(request);
    }

    @Override
    public DiffGeneralPerson diffPersonInfoFromCIFAndLocal(String personId) {
        ValidationUtils.checkBlankString(personId, () -> new MissingRequiredInputException("personId"));
        GeneralPerson localPersonInfo = findPersonByPersonId(Integer.parseInt(personId));
        ValidationUtils.checkNull(localPersonInfo, () -> new NoMatchRecordFoundException("local person not found"));
        PersonFindRequest request = createFindRequestFromLocalPerson(localPersonInfo);
        List<GeneralPerson> cifPersonInfoList = findCIFPersonInfo(request);
        ValidationUtils.checkNullOrEmptyList(cifPersonInfoList, () -> new NoMatchRecordFoundException("cif person not found"));
        if (cifPersonInfoList.size() > 1) {
            throw new TooManyRecordFoundException("cif person", cifPersonInfoList.size());
        }
        GeneralPerson cifPersonInfo = cifPersonInfoList.get(0);
        setCifUsername(cifPersonInfo);
        return comparedPerson(cifPersonInfo, localPersonInfo);
    }

    private PersonFindRequest createFindRequestFromLocalPerson(GeneralPerson localPersonInfo) {
        PersonFindRequest request = new PersonFindRequest();
        request.setNationality(localPersonInfo.getNationality());
        request.setPersonType(localPersonInfo.getPersonType());
        if (localPersonInfo instanceof GeneralRealPerson generalRealPerson) {
            request.setNationalId(generalRealPerson.getNationalCode());
        } else if (localPersonInfo instanceof GeneralLegalPerson legalPerson) {
            request.setNationalId(legalPerson.getNationalId());
            String subOrganizationId = legalPerson.getSubOrganizationId();
            if (StringUtils.isNotEmpty(subOrganizationId)) {
                request.setSubOrganizationId(subOrganizationId);
            }
        }
        return request;
    }

    private void setCifUsername(GeneralPerson cifPersonInfo) {
        if (cifPersonInfo instanceof GeneralRealPerson realPerson) {
            cifPersonInfo.setUsername(realPerson.getNationalCode());
        } else if (cifPersonInfo instanceof GeneralLegalPerson legalPerson) {
            String subOrganizationId = legalPerson.getSubOrganizationId();
            subOrganizationId = Objects.isNull(subOrganizationId) ? StringUtils.EMPTY : subOrganizationId;
            cifPersonInfo.setUsername(legalPerson.getNationalId() + subOrganizationId);
        }
    }

    private DiffGeneralPerson comparedPerson(GeneralPerson cifPersonInfo, GeneralPerson localPersonInfo) {
        Method[] allDeclaredMethods = ReflectionUtils.getAllDeclaredMethods(cifPersonInfo.getClass());
        DiffGeneralPerson diffGeneralPerson = new DiffGeneralPerson();
        List<DiffGeneralPerson.Diff> diffs = new ArrayList<>();
        diffGeneralPerson.setDiffs(diffs);
        diffGeneralPerson.setSyncAll(true);
        for (Method method : allDeclaredMethods) {
            String methodName = method.getName();
            if (methodName.startsWith("get") || methodName.startsWith("is")) {
                Object cifValue = ReflectionUtils.invokeMethod(method, cifPersonInfo);
                Object localValue;
                try {
                    localValue = ReflectionUtils.invokeMethod(Objects.requireNonNull(ReflectionUtils.findMethod(localPersonInfo.getClass(), methodName)), localPersonInfo);
                } catch (Exception e) {
                    localValue = null;
                }
                DiffGeneralPerson.Diff diff = new DiffGeneralPerson.Diff();
                diff.setSync(true);
                diff.setCurrent(localValue);
                diff.setUpdate(cifValue);
                diff.setTitle(methodName.replace("get", StringUtils.EMPTY).replace("is", StringUtils.EMPTY));
                if (!Objects.equals(cifValue, localValue) || !Objects.equals(localValue, cifValue)) {
                    diff.setSync(false);
                    diffGeneralPerson.setSyncAll(false);
                }
                diffs.add(diff);
            }
        }
        return diffGeneralPerson;
    }


    private GeneralPerson saveFoundCif(List<GeneralPerson> cifPersonInfo) {
        if (cifPersonInfo.size() > 1) {
            throw new TooManyRecordFoundException("cif person", cifPersonInfo.size());
        }
        GeneralPersonEntity personEntity = PersonMapper.INSTANCE.toPersonEntity(cifPersonInfo.get(0));
        personEntity.setUsername(extractUsername(personEntity));
        personEntity.setStatus(PersonStatus.ACTIVE);
        personEntity.setArchiveNo(ArchiveUtils.calculateTenYearsYearlyArchiveNo());
        List<GeneralPersonEntity> foundLocal = personRepository.findPersonByUsername(personEntity.getUsername());
        if (foundLocal.size() > 1) {
            throw new TooManyRecordFoundException("local person", cifPersonInfo.size());
        } else if (!foundLocal.isEmpty()) {
            personEntity.setId(foundLocal.get(0).getId());
        }
        personEntity = personRepository.save(personEntity);
        return PersonMapper.INSTANCE.toPerson(personEntity);
    }

    @Override
    public List<Role> findPersonRoleList(Long personId) {
        if (null == personId) {
            throw new MissingRequiredInputException("personId");
        }
        return RoleMapper.INSTANCE.toModels(roleRepository.findByPersonId(personId));
    }

    @Override
    public Role addPersonRole(Long personId, Integer roleId) {
        if (null == personId) {
            throw new MissingRequiredInputException("personId");
        }
        if (null == roleId) {
            throw new MissingRequiredInputException("roleId");
        }
        Optional<RoleEntity> roleEntity = roleRepository.findById(roleId);
        if (roleEntity.isEmpty()) {
            throw new InvalidInputException("roleId");
        }
        Optional<GeneralPersonEntity> personEntity = personRepository.findById(personId.intValue());
        if (personEntity.isEmpty()) {
            throw new InvalidInputException("personId");
        }
        roleRepository.insertPersonRole(personId, roleId);
        return RoleMapper.INSTANCE.toModel(roleEntity.get());
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
