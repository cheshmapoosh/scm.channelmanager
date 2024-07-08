package ir.daneshrefah.scm.process.service.util.convertor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ir.daneshrefah.scm.common.data.entity.person.CorporatePersonEntity;
import ir.daneshrefah.scm.common.data.entity.person.GeneralRealPersonEntity;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.process.exception.common.PersonNotFoundException;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static ir.daneshrefah.scm.common.model.person.PersonType.CORPORATE;
import static ir.daneshrefah.scm.common.model.person.PersonType.REAL;

@Service
@AllArgsConstructor
public class ToPersonFunctionDelegator implements FunctionDelegator<JsonNode, JsonNode> {

    private final PersonService personService;

    @Override
    public void init(JsonNode jsonNode) {
    }

    @Override
    public JsonNode apply(JsonNode jsonNode) {
        JsonNode customerIdNode = jsonNode.get("customerId");
        JsonNode typeNode = jsonNode.get("type");
        if (customerIdNode != null && StringUtils.isNotEmpty(customerIdNode.asText())) {
            return findByCustomerId(customerIdNode, typeNode);
        } else {
            return findByNationalId(jsonNode, typeNode);
        }
    }

    private JsonNode findByCustomerId(JsonNode customerIdNode, JsonNode typeNode) {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        objectMapper.registerModule(javaTimeModule);
        if (REAL.name().equalsIgnoreCase(typeNode.asText())) {
            Optional<GeneralPerson> generalPerson = personService.findPersonByPersonUsername(customerIdNode.asText());
            generalPerson.orElseThrow(() -> {
                throw new PersonNotFoundException("customerId", "Person not found with id: :id  " + customerIdNode.asText(), customerIdNode.asText());
            });
            return objectMapper.valueToTree(generalPerson.get());
        } else if (CORPORATE.name().equalsIgnoreCase(typeNode.asText())) {
            CorporatePersonEntity corporatePersonEntity = new CorporatePersonEntity();
            String customerId = customerIdNode.asText();
            corporatePersonEntity.setNationalId(customerId);
            corporatePersonEntity.setUsername(customerId);
            corporatePersonEntity.setPersonType(CORPORATE);
            return objectMapper.valueToTree(corporatePersonEntity);
        }
        throw new InvalidInputException("type");
    }

    private JsonNode findByNationalId(JsonNode jsonNode, JsonNode typeNode) {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        objectMapper.registerModule(javaTimeModule);
        objectMapper.findAndRegisterModules();
        if (typeNode != null && typeNode.isTextual() && StringUtils.isNotEmpty(typeNode.asText())) {
            String type = typeNode.asText();
            JsonNode nationalIdNode = jsonNode.get("nationalId");
            if (REAL.name().equalsIgnoreCase(type) && nationalIdNode != null) {
                GeneralRealPersonEntity generalRealPerson = personService.findPersonByNationalCode(nationalIdNode.asText());
                if (generalRealPerson == null) {
                    throw new PersonNotFoundException("nationalId", "Person not found with :id = " + nationalIdNode.asText(), nationalIdNode.asText());
                }
                return objectMapper.valueToTree(generalRealPerson);
            } else if (CORPORATE.name().equalsIgnoreCase(type)) {
                CorporatePersonEntity corporatePersonEntity = new CorporatePersonEntity();
                corporatePersonEntity.setNationalId(nationalIdNode.asText());
                corporatePersonEntity.setUsername(nationalIdNode.asText());
                corporatePersonEntity.setPersonType(CORPORATE);
                return objectMapper.valueToTree(corporatePersonEntity);
            }
        }
        throw new InvalidInputException("type");
    }
}