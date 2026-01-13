package ir.daneshrefah.scm.core.integration.template.context;

import ir.daneshrefah.scm.common.exception.AuthenticationRequiredException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.person.GeneralLegalPerson;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.model.person.GeneralRealPerson;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AuthenticatedPersonContextValueResolver implements ContextValueResolver {

    private static final String KEY = "auth.person.";

    @Override
    public boolean supports(String key) {
        return StringUtils.startsWithIgnoreCase(key, KEY);
    }

    @Override
    public Object resolve(String key, Exchange exchange) {
        GeneralPerson person = getCurrentPerson();
        var targetKey = StringUtils.replace(key, KEY, StringUtils.EMPTY);
        return switch (targetKey) {
            case "nationalId" -> getPersonNationalId(person);
            case "nationality" -> person.getNationality();
            case "nationality.code" -> person.getNationality().getCode();
            case "mobile" -> getPersonMobileNumber(person);
            case "username" -> person.getUsername();
            case "title" -> person.getTitle();
            case "type" -> person.getPersonType();
            case "id" -> person.getId();
            case "branchCode" -> person.getBranchCode();
            case "suborg" -> getSubOrganizationId(person);
            default -> throw new IllegalStateException("Unexpected value: " + key);
        };
    }

    private Object getSubOrganizationId(GeneralPerson person) {
        if (person instanceof GeneralLegalPerson legalPerson) {
            var subOrg = legalPerson.getSubOrganizationId();
            return StringUtils.isBlank(subOrg) ? "0" : subOrg.trim();
        }
        return  "0";
    }

    private Object getPersonMobileNumber(GeneralPerson person) {
        var mobile1 = person.getMobile1();
        if (StringUtils.isNotBlank(mobile1)) {
            return mobile1;
        }
        var mobile2 = person.getMobile2();
        if (StringUtils.isNotBlank(mobile2)) {
            return mobile2;
        }
        var mobile3 = person.getMobile3();
        if (StringUtils.isNotBlank(mobile3)) {
            return mobile3;
        }
        return null;
    }

    private String getPersonNationalId(GeneralPerson person) {
        if (person instanceof GeneralRealPerson realPerson) {
            return realPerson.getNationalCode();
        }
        if (person instanceof GeneralLegalPerson legalPerson) {
            return legalPerson.getNationalId();
        }
        return null;
    }

    private GeneralPerson getCurrentPerson() {
        var loggedInUser = AuthenticationUtils.getLoggedInUser();
        ValidationUtils.checkNull(loggedInUser, AuthenticationRequiredException::new);
        var person = Objects.requireNonNull(loggedInUser).getPerson();
        ValidationUtils.checkNull(person, () -> new NoMatchRecordFoundException("nationalId"));
        return person;
    }
}
