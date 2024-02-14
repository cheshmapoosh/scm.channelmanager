package ir.daneshrefah.scm.uaa.service.person;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import ir.daneshrefah.scm.common.data.service.person.PersonFindRequest;
import ir.daneshrefah.scm.common.exception.InvalidRemoteResponseException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-24
 */
@Service
public class NabCIFService implements CIFService {

    @Value("${scm.cif.url}")
    private String cifUrl;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public NabCIFService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(3000)) // Set connection timeout to 1 second
                .setReadTimeout(Duration.ofMillis(3000)) // Set read timeout to 2 seconds
                .build();
    }

    @Override
    public List<GeneralPerson> findPersonInfo(PersonFindRequest request) {
        if (null == request) {
            throw new MissingRequiredInputException("request body");
        }
        if (StringUtils.isEmpty(request.getNationalId())) {
            throw new MissingRequiredInputException("nationalId");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set(HttpConstants.HTTP_HEADER_AUTHORIZATION, "Bearer your_access_token");

        String nationalId = request.getNationalId();
        String subOrgId = StringUtils.isEmpty(request.getSubOrganizationId()) ? "0" : request.getSubOrganizationId();
        String requestBody = "{\"parameters\":[{\"name\":\"P_NATIONALID\",\"value\":\"" +
                nationalId +
                "\"},{\"name\":\"P_CUSTOMERTYPE\",\"value\":\"-1\"},{\"name\":\"P_SUBORGAN\",\"value\":\"" +
                subOrgId +
                "\"}],\"callType\":\"Reader\",\"encoding\":\"ASCII\",\"requestID\":\"RequestID\"}";
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        // Send POST request
        String url = cifUrl + "/SCMREAD.GETCUSTOMERNATIONALID";
        ResponseEntity<String> response = null;
        response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode().isError()) {
            throw new InvalidRemoteResponseException("CIF", "status code[" + response.getStatusCode().value() + "]");
        }
        JsonNode json = null;
        try {
            json = objectMapper.readTree(response.getBody());
        } catch (JsonProcessingException e) {
            throw new InvalidRemoteResponseException("CIF", e.getMessage(), e);
        }
        if (!json.has("result") || !json.get("result").isArray() || json.get("result").isEmpty()) {
            throw new NoMatchRecordFoundException("CIF", "nationalId [" + request.getNationalId() + "]");
        }
        ArrayNode resultNode = (ArrayNode) json.get("result");
        Iterator<JsonNode> iterator = resultNode.iterator();
        List<GeneralPerson> persons = new ArrayList<>();
        while (iterator.hasNext()) {
            JsonNode jsonNode = iterator.next();
            persons.add(NabCIFMapper.getInstance().toPerson(jsonNode));
        }
        return persons;
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
