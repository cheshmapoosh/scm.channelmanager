package ir.daneshrefah.scm.common.data.service.person;

import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.data.mapper.PersonMapper;
import ir.daneshrefah.scm.common.data.repository.PersonRepository;
import ir.daneshrefah.scm.common.data.repository.PersonSpecs;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.exception.PersonNotFoundException;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-27
 */
@RequiredArgsConstructor
public abstract class AbstractPersonServiceDatabaseImpl implements PersonService {

    protected final PersonRepository personRepository;

    @Override
    public PagedResponseData<GeneralPerson> findPagedPersonList(PersonFindRequest request) {
        if (null == request) {
            request = new PersonFindRequest();
        }
        Pageable pageable = PageRequest.of(Math.max(request.getPageNo() - 1, 0), request.getPageSize());
        Page<GeneralPersonEntity> entities = personRepository.findAll(PersonSpecs.toSpecification(request), pageable);
        return new PagedResponseData<>(request.getPageNo(), request.getPageSize(), entities.getTotalElements(),
                PersonMapper.INSTANCE.toModels(entities.getContent()));
    }

    @Override
    public boolean checkPersonExist(PersonFindRequest request) {
        if (null == request) {
            request = new PersonFindRequest();
        }
        return personRepository.exists(PersonSpecs.toSpecification(request));
    }

    @Override
    public GeneralPerson findPersonByPersonId(Integer id) {
        Optional<GeneralPersonEntity> personEntity = personRepository.findById(id);
        if (personEntity.isEmpty()) {
            throw new PersonNotFoundException("person with id '" + id + "' not found.");
        }
        return PersonMapper.INSTANCE.toPerson(personEntity.get());
    }

    /*private final ServiceProducerTemplate serviceProducerTemplate;
    private final ErrorHandlerService errorHandlerService;

    @Override
    public final GeneralPerson findCIFPersonInfo(PersonFindRequest request) {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("customerType", "-1");
        payload.put("nationalId", request.getNationalId());
        payload.put("subOrganizationId", null != request.getSubOrganizationId() ? request.getSubOrganizationId() : "0");
        Message message = serviceProducerTemplate.callService("SVC_NAB_FIND_CUSTOMER", payload);
        if (null != message.getErrors() && !message.getErrors().isEmpty()) {
            BaseException exception = errorHandlerService.resolveExceptionByError(message);
            if (null != exception) {
                throw exception;
            }
        }
        JsonNode personNode = message.getPayload();
        if (null == personNode || personNode.isNull() || personNode.isEmpty()) {
            return null;
        }
        if (personNode.isArray() && !personNode.isEmpty()) {
            personNode = personNode.get(0);
        }
        return PersonCIFMapper.getInstance().toPerson(personNode);
    }*/

    /*public GeneralPerson saveOrUpdateLocalPersonInfoFromCIF(PersonFindRequest request) {
        if (null == request.getPersonType()) {
            throw new ValidationException("personType", ERROR_CODE_VALIDATION_PERSON_TYPE_IS_INVALID, "personType is invalid.");
        }
        if (StringUtils.isEmpty(request.getNationalId())) {
            throw new ValidationException("nationalId", ERROR_CODE_VALIDATION_PERSON_NATIONAL_ID_IS_EMPTY, "nationalId is empty.");
        }
        GeneralPerson result = null;
        GeneralPerson remotePerson = findCIFPersonInfo(request);
        if (null == remotePerson) {
            throw new ResultNotFoundException("person", ERROR_CODE_VALIDATION_PERSON_NOT_FOUND, "CIF person not found.");
        }
        GeneralPerson localPerson = findPersonInfo(request);
        if (null == localPerson || null == localPerson.getId()) {
            result = savePerson(remotePerson);
        } else {
            remotePerson.setId(localPerson.getId());
            result = updatePerson(remotePerson);
        }
        return result;
    }*/

}
