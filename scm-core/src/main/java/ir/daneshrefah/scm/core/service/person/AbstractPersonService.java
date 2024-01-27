package ir.daneshrefah.scm.core.service.person;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.data.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.data.type.Nationality;
import ir.daneshrefah.scm.common.data.type.PersonType;
import ir.daneshrefah.scm.common.exception.BaseException;
import ir.daneshrefah.scm.common.exception.ValidationException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.plugin.api.integration.ErrorHandlerService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.PersonService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-27
 */
@RequiredArgsConstructor
public abstract class AbstractPersonService implements PersonService {

    private final ServiceProducerTemplate serviceProducerTemplate;
    private final ErrorHandlerService errorHandlerService;

    public final GeneralPerson inquirePersonFromCIFByPersonInfo(PersonType personType, Nationality nationality,
                                                                String nationalId, String subOrganizationId) {
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("customerType", "-1");
        payload.put("nationalId", nationalId);
        payload.put("subOrganizationId", null != subOrganizationId ? subOrganizationId : "0");
        Message message = serviceProducerTemplate.callService("SVC_NAB_FIND_CUSTOMER", payload);
        if (null != message.getErrors() && !message.getErrors().isEmpty()) {
            BaseException exception = errorHandlerService.resolveExceptionByError(message);
            if (null != exception) {
                throw exception;
            }
        }
        JsonNode personNode = message.getPayload();
        if (null == personNode || personNode.isNull()) {
            throw new ValidationException("person", ERROR_CODE_VALIDATION_PERSON_NOT_FOUND,
                    String.format("person not found for personType: '%s', nationality: '%s', nationalId: '%s', subOrganizationId: '%s'.",
                            null != personType ? personType.getCode() : "null",
                            null != nationality ? nationality.getCode() : "null", nationalId, subOrganizationId));
        }
        if (personNode.isArray() && !personNode.isEmpty()) {
            personNode = personNode.get(0);
        }
        return PersonCIFMapper.getInstance().toPerson(personNode);
    }

    public GeneralPerson defineOrUpdatePersonInfo(PersonType personType, Nationality nationality, String nationalId, String subOrganizationId) {
        if (null == personType) {
            throw new ValidationException("personType", ERROR_CODE_VALIDATION_PERSON_TYPE_IS_INVALID, "personType is invalid.");
        }
        if (StringUtils.isEmpty(nationalId)) {
            throw new ValidationException("nationalId", ERROR_CODE_VALIDATION_PERSON_NATIONAL_ID_IS_EMPTY, "nationalId is empty.");
        }
        GeneralPerson remotePerson = inquirePersonFromCIFByPersonInfo(personType, nationality, nationalId, subOrganizationId);
        GeneralPerson localPerson = findPersonByPersonInfo(personType, nationality, nationalId, subOrganizationId);
        return null;
    }

}
