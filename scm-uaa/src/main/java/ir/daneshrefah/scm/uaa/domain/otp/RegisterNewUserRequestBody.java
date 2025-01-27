package ir.daneshrefah.scm.uaa.domain.otp;

import ir.daneshrefah.scm.common.data.entity.person.GeneralLegalPersonEntity;
import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.data.entity.person.GeneralRealPersonEntity;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.uaa.exception.OtpServiceException;
import ir.daneshrefah.scm.uaa.repository.authentication.UserEntity;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class RegisterNewUserRequestBody extends Message {

    private String nationalCode;
    private String email;
    private String username;
    private String zoneCode;
    private String firstName;
    private String lastName;
    @Getter
    private String branchCode;
    private String channelId;
    private String authenticationMode;
    private String tokenType;
    private static final int NATIONAL_CODE_INDEX = 0;
    private static final int FIRST_NAME_INDEX = 1;
    private static final int LAST_NAME_INDEX = 2;
    private static final int EMAIL_INDEX = 3;
    private static final int BRANCH_CODE_INDEX = 4;
    private static final int AUTHENTICATION_MODE_INDEX = 5;
    private static final int ZONE_CODE_INDEX = 6;
    private static final int USER_NAME_INDEX = 7;
    private static final int CHANNEL_ID_INDEX = 8;
    private static final int TOKEN_TYPE_INDEX = 9;
    private static final int FIELDS_NO = 10;
    private final String DEFAULT_EMAIL = "a@dpi.ir";
    private final String DEFAULT_TOKEN_TYPE = "DEVICE";

    public RegisterNewUserRequestBody(UserEntity user, String authenticationMode, String channelId, String tokenType, String employeeBranchCode) {
        this.authenticationMode = authenticationMode;
        validateBlankString(this.authenticationMode, "authenticationMode");
        this.channelId = channelId;
        validateBlankString(this.channelId, "channelId");
        GeneralPersonEntity person = user.getPerson();
        PersonType personType = person.getPersonType();
        this.branchCode = person.getBranchCode();
        validateBlankString(this.branchCode, "branchCode");
        this.zoneCode = employeeBranchCode;
        validateBlankString(this.zoneCode, "zoneCode");
        this.username = person.getUsername();
        this.email = (person.getEmail() == null || StringUtils.isEmpty(person.getEmail())) ? DEFAULT_EMAIL : person.getEmail();
        validateBlankString(this.email, "email");
        this.tokenType = (tokenType == null || StringUtils.isEmpty(tokenType)) ? DEFAULT_TOKEN_TYPE : tokenType;
        validateBlankString(this.tokenType, "tokenType");
        switch (personType) {
            case BANK:
            case GOVERNANCE:
            case CORPORATE:
            case TAMIN:
                GeneralLegalPersonEntity generalLegalPerson = (GeneralLegalPersonEntity) person;
                this.firstName = generalLegalPerson.getTitle();
                validateBlankString(this.firstName, "firstName");
                this.lastName = generalLegalPerson.getTitleEnglish();
                validateBlankString(this.lastName, "lastName");
                this.nationalCode = generalLegalPerson.getNationalId();
                validateBlankString(this.nationalCode, "nationalCode");
                break;
            case REAL:
            case EMPLOYEE:
                GeneralRealPersonEntity generalRealPerson = (GeneralRealPersonEntity) person;
                this.firstName = generalRealPerson.getFirstName();
                validateBlankString(this.firstName, "firstName");
                this.lastName = generalRealPerson.getLastName();
                validateBlankString(this.lastName, "lastName");
                this.nationalCode = generalRealPerson.getNationalCode();
                validateBlankString(this.nationalCode, "nationalCode");
                break;
        }
    }

    private static void validateBlankString(String requiredStr, String source) {
        ValidationUtils.checkBlankString(requiredStr, () -> new MissingRequiredInputException(source));
    }

    public List<String> toList() {
        List<String> registerNewUserRequestBodyAsList = new ArrayList();
        registerNewUserRequestBodyAsList.add(nationalCode);
        registerNewUserRequestBodyAsList.add(firstName);
        registerNewUserRequestBodyAsList.add(lastName);
        registerNewUserRequestBodyAsList.add(email);
        registerNewUserRequestBodyAsList.add(branchCode);
        registerNewUserRequestBodyAsList.add(zoneCode);
        registerNewUserRequestBodyAsList.add(authenticationMode);
        registerNewUserRequestBodyAsList.add(username);
        registerNewUserRequestBodyAsList.add(channelId);
        registerNewUserRequestBodyAsList.add(tokenType);
        return registerNewUserRequestBodyAsList;
    }

    public void setFieldsFromList(List<String> listMessage) {
        if (!isSizeOfInputListEqualGreaterThanFieldNo(listMessage)) {
            throw new OtpServiceException();
        } else {
            this.nationalCode = listMessage.get(NATIONAL_CODE_INDEX);
            this.firstName = listMessage.get(FIRST_NAME_INDEX);
            this.lastName = listMessage.get(LAST_NAME_INDEX);
            this.branchCode = listMessage.get(BRANCH_CODE_INDEX);
            this.authenticationMode = listMessage.get(AUTHENTICATION_MODE_INDEX);
            this.channelId = listMessage.get(CHANNEL_ID_INDEX);
            this.zoneCode = listMessage.get(ZONE_CODE_INDEX);
            this.email = listMessage.get(EMAIL_INDEX);
            this.username = listMessage.get(USER_NAME_INDEX);
            this.tokenType = listMessage.get(TOKEN_TYPE_INDEX);
        }
    }

    @Override
    int getFieldsNo() {
        return FIELDS_NO;
    }
}
