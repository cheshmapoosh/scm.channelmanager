package ir.daneshrefah.scm.process.service.util.convertor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ir.daneshrefah.scm.common.data.entity.person.CorporatePersonEntity;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.model.person.GeneralRealPerson;
import ir.daneshrefah.scm.process.exception.common.PersonNotFoundException;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static ir.daneshrefah.scm.common.model.person.PersonType.CORPORATE;
import static ir.daneshrefah.scm.common.model.person.PersonType.REAL;

@Service
public class ToPersonFunctionDelegator implements FunctionDelegator<JsonNode, JsonNode> {

    private final PersonService personService;
    private final ObjectMapper objectMapper;

    public ToPersonFunctionDelegator(PersonService personService, ObjectMapper objectMapper) {
        this.personService = personService;
        this.objectMapper = objectMapper;
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.findAndRegisterModules();
    }

    @Override
    public void init(JsonNode jsonNode) {
    }

    @Override
    public JsonNode apply(JsonNode jsonNode) {
        if (!jsonNode.isObject()) {
            return findByNationalId(jsonNode);
        }
        JsonNode customerIdNode = jsonNode.get("customerId");
        JsonNode typeNode = jsonNode.get("type");
        if (customerIdNode != null && StringUtils.isNotEmpty(customerIdNode.asText())) {
            return findByCustomerId(customerIdNode, typeNode);
        } else {
            return findByNationalIdAndType(jsonNode, typeNode);
        }
    }

    private JsonNode findByCustomerId(JsonNode customerIdNode, JsonNode typeNode) {
        String type = typeNode.asText();
        String customerId = customerIdNode.asText();
        if (REAL.name().equalsIgnoreCase(type)) {
            return findRealPersonByUsername(customerId);
        } else if (CORPORATE.name().equalsIgnoreCase(type)) {
            return createCorporatePersonEntity(customerId);
        } else {
            throw new InvalidInputException("type");
        }
    }

    private JsonNode findByNationalIdAndType(JsonNode jsonNode, JsonNode typeNode) {
        if (typeNode == null || !typeNode.isTextual() || StringUtils.isEmpty(typeNode.asText())) {
            throw new InvalidInputException("type");
        }
        String type = typeNode.asText();
        JsonNode nationalIdNode = jsonNode.get("nationalId");
        if (REAL.name().equalsIgnoreCase(type) && nationalIdNode != null) {
            return findRealPersonByNationalCode(nationalIdNode.asText());
        } else if (CORPORATE.name().equalsIgnoreCase(type)) {
            return createCorporatePersonEntity(nationalIdNode.asText());
        } else {
            throw new InvalidInputException("type");
        }
    }

    private JsonNode findByNationalId(JsonNode jsonNode) {
        if (jsonNode == null || !jsonNode.isTextual() || StringUtils.isEmpty(jsonNode.asText())) {
            throw new InvalidInputException("");
        }
        return findRealPersonByNationalCode(jsonNode.asText());
    }

    private JsonNode findRealPersonByUsername(String username) {
        Optional<GeneralPerson> generalPerson = personService.findPersonByPersonUsername(username);
        if (generalPerson.isEmpty()) {
            throw new PersonNotFoundException("customerId", "Person not found with id: " + username, username);
        }
        return objectMapper.valueToTree(generalPerson.get());
    }

    private JsonNode findRealPersonByNationalCode(String nationalId) {
        GeneralRealPerson generalRealPerson = personService.findPersonByNationalCode(nationalId);
        if (generalRealPerson == null) {
            throw new PersonNotFoundException("nationalId", "Person not found with id: " + nationalId, nationalId);
        }
        return objectMapper.valueToTree(generalRealPerson);
    }

    private JsonNode createCorporatePersonEntity(String id) {
        CorporatePersonEntity corporatePersonEntity = new CorporatePersonEntity();
        corporatePersonEntity.setNationalId(id);
        corporatePersonEntity.setUsername(id);
        corporatePersonEntity.setPersonType(CORPORATE);
        return objectMapper.valueToTree(corporatePersonEntity);
    }
}