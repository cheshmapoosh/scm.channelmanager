package ir.daneshrefah.scm.uaa.service.person;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.exception.ValidationException;
import ir.daneshrefah.scm.common.model.person.*;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.utils.date.DateUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;

import java.time.LocalDate;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_VALIDATION_PERSON_TYPE_IS_EMPTY;
import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_VALIDATION_PERSON_TYPE_IS_INVALID;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-27
 */
public class NabCIFMapper {

    private static final String PROP_CUSTOMER_TYPE = "CUSTOMERTYPE";
    private static final String PROP_NATIONALITY = "ISFOREIGN";
    private static final String PROP_MOBILE1 = "MOBILE";
    private static final String PROP_MOBILE2 = "MOBILE1";
    private static final String PROP_MOBILE3 = "MOBILE2";
    private static final String PROP_TEL1 = "TEL1";
    private static final String PROP_TEL2 = "TEL2";
    private static final String PROP_ADDRESS1 = "ADDRESS1";
    private static final String PROP_ADDRESS2 = "ADDRESS2";
    private static final String PROP_ADDRESS3 = "ADDRESS3";
    private static final String PROP_ADDRESS4 = "ADDRESS4";
    private static final String PROP_POST1 = "POST1";
    private static final String PROP_POST2 = "POST2";
    private static final String PROP_FAX = "CUSTOMERFAX";
    private static final String PROP_EMAIL = "CUSTOMEREMAIL";

    private static final NabCIFMapper INSTANCE = new NabCIFMapper();

    public GeneralPerson toPerson(JsonNode personNode) {
        ObjectNode personObjectNode = personNode.isObject() ? (ObjectNode) personNode : null;
        if (null == personObjectNode) {
            return null;
        }
        if (!personObjectNode.has(PROP_CUSTOMER_TYPE) || !personObjectNode.get(PROP_CUSTOMER_TYPE).isNumber()) {
            throw new ValidationException("CIF", ERROR_CODE_VALIDATION_PERSON_TYPE_IS_EMPTY, "remote personType is empty.");
        }
        Integer customerTypeCode = personObjectNode.get(PROP_CUSTOMER_TYPE).asInt();
        GeneralPerson result = null;
        switch (customerTypeCode) {
            case Constants.CIF_PERSON_TYPE_REAL:
                result = generateRealPerson(personObjectNode);
                break;
            case Constants.CIF_PERSON_TYPE_CORPORATE:
                result = generateCorporatePerson(personObjectNode);
                break;
            case Constants.CIF_PERSON_TYPE_BANK:
                result = generateBankPerson(personObjectNode);
                break;
            case Constants.CIF_PERSON_TYPE_GOVERNANCE:
                result = generateGovernancePerson(personObjectNode);
                break;
            case Constants.CIF_PERSON_TYPE_TAMIN:
                result = generateTaminPerson(personObjectNode);
                break;
            default:
                throw new ValidationException("CIF", ERROR_CODE_VALIDATION_PERSON_TYPE_IS_INVALID,
                        "remote personType is invalid '" + customerTypeCode + "'.");
        }
//        private String username;
//        private Boolean active;
        result.setNationality(personNode.get(PROP_NATIONALITY).asBoolean() ? Nationality.FOREIGN : Nationality.IRANIAN);
        result.setMobile1(personNode.get(PROP_MOBILE1).asText());
        result.setMobile2(extractStringValue(personNode, PROP_MOBILE2));
        result.setMobile3(extractStringValue(personNode, PROP_MOBILE3));
        result.setPhone1(extractStringValue(personNode, PROP_TEL1));
        result.setPhone2(extractStringValue(personNode, PROP_TEL2));
        result.setAddress1(personNode.get(PROP_ADDRESS1).asText());
        result.setAddress2(personNode.get(PROP_ADDRESS2).asText());
        result.setAddress3(personNode.get(PROP_ADDRESS3).asText());
        result.setAddress4(personNode.get(PROP_ADDRESS4).asText());
        result.setPostalCode1(extractStringValue(personNode, PROP_POST1));
        result.setPostalCode2(extractStringValue(personNode, PROP_POST2));
        result.setFax(extractStringValue(personNode, PROP_FAX));
        result.setEmail(extractStringValue(personNode, PROP_EMAIL));
        result.setShahabCode(extractStringValue(personNode, "SHAHABCODE"));
        result.setBranchCode(extractStringValue(personNode, "BRANCHCODE"));
        result.setRegisterIssueDate(extractDateValue(personNode, "REGISSUDATE"));
        return result;

    }

    private LocalDate extractDateValue(JsonNode node, String key) {
        if (!node.has(key) || !StringUtils.isNumeric(node.get(key).asText()) ||
                Integer.valueOf(node.get(key).asText()) < 1) {
            return null;
        }
        return DateUtils.ShamsiCalendarConvertor.convertCompactToLocalDate(node.get(key).asText());
    }
    private String extractStringValue(JsonNode node, String key) {
        if (!node.has(key) || StringUtils.isEmpty(node.get(key).asText())) {
            return null;
        }
        return node.get(key).asText();
    }

    private TaminPerson generateTaminPerson(ObjectNode personNode) {
        if (null == personNode || personNode.isNull() || !personNode.isObject()) {
            return null;
        }
        TaminPerson result = new TaminPerson();
        return result;
    }

    private GovernancePerson generateGovernancePerson(ObjectNode personNode) {
        if (null == personNode || personNode.isNull() || !personNode.isObject()) {
            return null;
        }
        GovernancePerson result = new GovernancePerson();
        return result;
    }

    private BankPerson generateBankPerson(ObjectNode personNode) {
        if (null == personNode || personNode.isNull() || !personNode.isObject()) {
            return null;
        }
        BankPerson result = new BankPerson();
        return result;
    }

    private CorporatePerson generateCorporatePerson(ObjectNode personNode) {
        if (null == personNode || personNode.isNull() || !personNode.isObject()) {
            return null;
        }
        CorporatePerson result = new CorporatePerson();
        result.setNationalId(personNode.get("NATIONALID").asText());
        result.setSubOrganizationId(personNode.get("SUBORGAN").asText());
        result.setRegisterDate(extractDateValue(personNode, "BIRTHDATE"));
        return result;
    }

    private IndividualPerson generateRealPerson(ObjectNode personNode) {
        if (null == personNode || personNode.isNull() || !personNode.isObject()) {
            return null;
        }
        IndividualPerson result = new IndividualPerson();
        result.setFirstName(personNode.get("FIRSTNAME").asText());
        result.setFirstNameEnglish(personNode.get("NAME_LATIN").asText());
        result.setLastName(personNode.get("LASTNAME").asText());
        result.setLastNameEnglish(personNode.get("TITLEX_LATIN").asText());
        result.setFatherName(personNode.get("FATHERNAME").asText());
        result.setNationalCode(personNode.get("NATIONALID").asText());
        result.setMaritalStatus(Constants.CIF_MARITAL_STATUS_MARRIED == personNode.get("MARRIEDSTATUSCODE").asInt() ?
                MaritalStatus.MARRIED : MaritalStatus.SINGLE);
        result.setMaritalStatusTitle(personNode.get("MARRIEDSTATUS").asText());
        result.setGender(Gender.findByCode(personNode.get("GENDERCODE").asInt()));
        result.setGenderTitle(extractStringValue(personNode, "GENDER"));
        result.setBirthDate(extractDateValue(personNode, "BIRTHDATE"));
        result.setDeadDate(extractDateValue(personNode, "DEADDATE"));
        result.setIdentificationNo(personNode.get("REGISTERID").asText());
        result.setIdentificationSeries(personNode.get("SERI").asText());
        result.setIdentificationSerial(personNode.get("SERIAL").asText());
        result.setRegisterIssueDate(extractDateValue(personNode, "REGISSUDATE"));
        result.setJobCode(StringUtils.leftPadZero(extractStringValue(personNode, "CUSTOMERJOB"), 4));
        result.setJobTitle(personNode.get("JOBTITLE").asText());
        result.setEducationCode(personNode.get("CUSTOMEREDUCATION").asText());
        result.setEducationTitle(personNode.get("EDUCATIONTITLE").asText());
        result.setMajorCode(personNode.get("CUSTOMERCOURSE").asText());
        result.setMajorTitle(personNode.get("COURSETITLE").asText());
//        result.setStateCode(personNode.get("stateCode").asText());
//        result.setStateTitle(personNode.get("stateTitle").asText());
//        result.setCityCode(personNode.get("cityCode").asText());
//        result.setCityTitle(personNode.get("cityTitle").asText());
        result.setIdentificationDocumentTypeCode(personNode.get("CUSTOMERDOC").asText());
        result.setIdentificationDocumentTypeTitle(personNode.get("DOCTITLE").asText());
        result.setLived(personNode.get("CUSTOMERISLIVED").asBoolean());

        return result;
    }

    public static NabCIFMapper getInstance() {
        return INSTANCE;
    }

}
