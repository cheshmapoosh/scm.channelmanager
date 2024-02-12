package ir.daneshrefah.scm.core.service.person;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.data.model.person.CorporatePerson;
import ir.daneshrefah.scm.common.data.model.person.EmployeePerson;
import ir.daneshrefah.scm.common.data.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.data.model.person.IndividualPerson;
import ir.daneshrefah.scm.common.data.type.MaritalStatus;
import ir.daneshrefah.scm.common.data.type.Nationality;
import ir.daneshrefah.scm.common.exception.ValidationException;
import ir.daneshrefah.scm.utils.date.DateUtils;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_VALIDATION_PERSON_TYPE_IS_EMPTY;
import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_VALIDATION_PERSON_TYPE_IS_INVALID;
import static ir.daneshrefah.scm.uaa.common.utils.Constants.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-27
 */
public class PersonCIFMapper {

    private static final PersonCIFMapper INSTANCE = new PersonCIFMapper();

    public GeneralPerson toPerson(JsonNode personNode) {
        ObjectNode personObjectNode = personNode.isObject() ? (ObjectNode) personNode : null;
        if (null == personObjectNode) {
            return null;
        }
        if (!personObjectNode.has("customerType") || !personObjectNode.get("customerType").isNumber()) {
            throw new ValidationException("CIF", ERROR_CODE_VALIDATION_PERSON_TYPE_IS_EMPTY, "remote personType is empty.");
        }
        Integer customerTypeCode = personObjectNode.get("customerType").asInt();
        /*PersonType customerType = PersonType.findByCode(customerTypeCode);
        if (null == customerType) {
            throw new ValidationException("CIF", ERROR_CODE_VALIDATION_PERSON_TYPE_IS_INVALID,
                    "remote personType is invalid '" + customerTypeCode + "'.");
        }*/
        GeneralPerson result = null;
        switch (customerTypeCode) {
            case CIF_PERSON_TYPE_INDIVIDUAL:
                result = generateIndividualCustomerPerson(personObjectNode);
                break;
            /*case EMPLOYEE:
                result = generateEmployeePerson(personObjectNode);
                break;*/
            case CIF_PERSON_TYPE_CORPORATE:
                result = generateCorporateCustomerPerson(personObjectNode);
                break;
            default:
                throw new ValidationException("CIF", ERROR_CODE_VALIDATION_PERSON_TYPE_IS_INVALID,
                        "remote personType is invalid '" + customerTypeCode + "'.");
        }
//        private String username;
//        private Boolean active;
        result.setNationality(personNode.get("isForeign").asBoolean() ? Nationality.FOREIGN : Nationality.IRANIAN);
        result.setMobile1(personNode.get("mobile1").asText());
        result.setMobile2(personNode.get("mobile2").asText());
        result.setMobile3(personNode.get("mobile3").asText());
        result.setPhone1(personNode.get("tel1").asText());
        result.setPhone2(personNode.get("tel2").asText());
        result.setAddress1(personNode.get("address1").asText());
        result.setAddress2(personNode.get("address2").asText());
        result.setAddress3(personNode.get("address3").asText());
        result.setAddress4(personNode.get("address4").asText());
        result.setPostalCode1(personNode.get("postalCode1").asText());
        result.setPostalCode2(personNode.get("postalCode2").asText());
        result.setFax(personNode.get("fax").asText());
        result.setEmail(personNode.get("email").asText());
        return result;

    }

    private CorporatePerson generateCorporateCustomerPerson(ObjectNode personNode) {
        if (null == personNode || personNode.isNull() || !personNode.isObject()) {
            return null;
        }
        CorporatePerson result = new CorporatePerson();
        return result;

    }

    private EmployeePerson generateEmployeePerson(ObjectNode personNode) {
        if (null == personNode || personNode.isNull() || !personNode.isObject()) {
            return null;
        }
        EmployeePerson result = new EmployeePerson();
        return result;
    }

    private IndividualPerson generateIndividualCustomerPerson(ObjectNode personNode) {
        if (null == personNode || personNode.isNull() || !personNode.isObject()) {
            return null;
        }
        IndividualPerson result = new IndividualPerson();
        result.setFirstName(personNode.get("firstName").asText());
        result.setFirstNameEnglish(personNode.get("firstNameEnglish").asText());
        result.setLastName(personNode.get("lastName").asText());
        result.setLastNameEnglish(personNode.get("lastNameEnglish").asText());
        result.setFatherName(personNode.get("fatherName").asText());
        result.setNationalCode(personNode.get("nationalId").asText());
        result.setMaritalStatus(CIF_MARITAL_STATUS_MARRIED == personNode.get("maritalStatusCode").asInt() ?
                MaritalStatus.MARRIED : MaritalStatus.SINGLE);
//        result.setGender(Gender.findByCode(personNode.get("genderCode").asInt()));
        result.setBirthDate(DateUtils.ShamsiCalendarConvertor.convertCompactToLocalDate(personNode.get("birthDate").asText()));
        result.setDeadDate(DateUtils.ShamsiCalendarConvertor.convertCompactToLocalDate(personNode.get("deadDate").asText()));
        result.setIdentificationNo(personNode.get("registerId").asText());
        result.setRegisterSeries(personNode.get("registerSeries").asText());
        result.setRegisterSerial(personNode.get("registerSerial").asText());
//        result.setRegisterIssueDate(personNode.get("registerIssueDate").asText());
        result.setJobCode(personNode.get("jobCode").asText());
        result.setJobTitle(personNode.get("jobTitle").asText());
        result.setEducationCode(personNode.get("educationCode").asText());
        result.setEducationTitle(personNode.get("educationTitle").asText());
        result.setStateCode(personNode.get("stateCode").asText());
        result.setStateTitle(personNode.get("stateTitle").asText());
        result.setCityCode(personNode.get("cityCode").asText());
        result.setCityTitle(personNode.get("cityTitle").asText());
        result.setBranchCode(personNode.get("branchCode").asText());
        result.setShahabCode(personNode.get("shahabCode").asText());
        result.setCourseCode(personNode.get("courseCode").asText());
        result.setCourseTitle(personNode.get("courseTitle").asText());
        result.setDocumentTypeCode(personNode.get("documentTypeCode").asText());
        result.setDocumentTypeTitle(personNode.get("documentTypeTitle").asText());
//        result.setLived(personNode.get("isLived").asText());

        return result;
    }

    public static PersonCIFMapper getInstance() {
        return INSTANCE;
    }

}
