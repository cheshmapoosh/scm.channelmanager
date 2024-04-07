package ir.daneshrefah.scm.uaa.common.token;

import ir.daneshrefah.scm.common.model.person.Nationality;
import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.common.model.person.*;
import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.net.URL;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-25
 */
public class JwtTokenConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public UserAuthentication convert(Jwt jwt) {
        String clientId = jwt.getAudience().get(0);

        Collection<GrantedAuthority> authorities = Collections.emptyList();
        String commaSeparatedAuthorities = jwt.getClaimAsString(Constants.CLAIM_KEY_AUTHORITIES);
        if (StringUtils.isNotEmpty(commaSeparatedAuthorities)) {
            String[] authoritiesArray = commaSeparatedAuthorities.split(",");

            authorities = Arrays.stream(authoritiesArray)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
        URL issuer = jwt.getIssuer(); //JwtClaimNames.ISS
        String sessionId = jwt.getClaimAsString(Constants.CLAIM_KEY_SESSION);
        Instant issuedAt = jwt.getIssuedAt();
        Instant expiresAt = jwt.getExpiresAt();

        User user = extractUserFromJwt(jwt);

        UserAuthentication.AuthenticationDetail detail = UserAuthentication.AuthenticationDetail.builder()
                .issuer(issuer.toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .maxIdle(null)
                .loginData(jwt)
                .loginAccessParameter(null)
                .sessionId(sessionId)
                .clientId(clientId)
                .build();

        UserAuthentication result = new UserAuthentication(detail,
                user, authorities);

        return result;
    }

    private User extractUserFromJwt(Jwt jwt) {
        String clientId = jwt.getAudience().get(0);
        AuthorizationGrantType grantType = AuthorizationGrantType.valueOf(jwt.getClaimAsString(Constants.CLAIM_KEY_GRANT));
        PersonType personType = PersonType.findByCode(Integer.valueOf(jwt.getClaimAsString(Constants.CLAIM_KEY_PERSON_TYPE)));
        GeneralPerson person = null;
        switch (personType) {
            case REAL:
                person = new IndividualPerson();
                ((IndividualPerson) person).setNationalCode(jwt.getClaimAsString(Constants.CLAIM_KEY_PERSON_NATIONAL_ID));
                ((IndividualPerson) person).setFirstName(jwt.getClaimAsString(Constants.CLAIM_KEY_PERSON_FIRST_NAME));
                ((IndividualPerson) person).setLastName(jwt.getClaimAsString(Constants.CLAIM_KEY_PERSON_LAST_NAME));
                break;
            case EMPLOYEE:
                person = new EmployeePerson();
                ((EmployeePerson) person).setNationalCode(jwt.getClaimAsString(Constants.CLAIM_KEY_PERSON_NATIONAL_ID));
                ((EmployeePerson) person).setFirstName(jwt.getClaimAsString(Constants.CLAIM_KEY_PERSON_FIRST_NAME));
                ((EmployeePerson) person).setLastName(jwt.getClaimAsString(Constants.CLAIM_KEY_PERSON_LAST_NAME));
                break;
            case CORPORATE:
                person = new CorporatePerson();
                ((GeneralLegalPerson) person).setNationalId(jwt.getClaimAsString(Constants.CLAIM_KEY_PERSON_NATIONAL_ID));
                ((GeneralLegalPerson) person).setSubOrganizationId(jwt.getClaimAsString(Constants.CLAIM_KEY_PERSON_SUB_ORGANIZATION_ID));
                ((GeneralLegalPerson) person).setTitle(jwt.getClaimAsString(Constants.CLAIM_KEY_PERSON_TITLE));
                break;
            case CLIENT:
                person = new ClientPerson();
                break;
        }
        person.setUsername(jwt.getClaimAsString(Constants.CLAIM_KEY_PERSON_PROFILE_IDENTIFIER));
        if (jwt.hasClaim(Constants.CLAIM_KEY_PERSON_IDENTIFIER)) {
            person.setId(Integer.valueOf(jwt.getClaimAsString(Constants.CLAIM_KEY_PERSON_IDENTIFIER)));
        }
        person.setNationality(Nationality.findByCode(jwt.getClaimAsString(Constants.CLAIM_KEY_PERSON_NATIONALITY)));

        String terminalCode = jwt.getClaimAsString(Constants.CLAIM_KEY_TERMINAL);
        AuthenticationMethod loginAuthenticationMethod = null;
        if (StringUtils.isNotEmpty(jwt.getClaimAsString(Constants.CLAIM_KEY_LOGIN_AUTH_METHOD))) {
            loginAuthenticationMethod = AuthenticationMethod.findByCode(
                    jwt.getClaimAsString(Constants.CLAIM_KEY_LOGIN_AUTH_METHOD));
        }
        AuthenticationMethod transactionAuthenticationMethod = null;
        if (StringUtils.isNotEmpty(jwt.getClaimAsString(Constants.CLAIM_KEY_TRANSACTION_AUTH_METHOD))) {
            transactionAuthenticationMethod = AuthenticationMethod.findByCode(
                    jwt.getClaimAsString(Constants.CLAIM_KEY_TRANSACTION_AUTH_METHOD));
        }
        String username = StringUtils.isNotEmpty(jwt.getSubject()) ? jwt.getSubject() : clientId;

        User user = new User();
        user.setTerminalCode(terminalCode);
        user.setNickname(username);
        user.setLoginAuthenticationMethod(loginAuthenticationMethod);
        user.setTransactionAuthenticationMethod(transactionAuthenticationMethod);
        user.setActive(true);
        user.setPerson(person);

        Set<String> authorities = Collections.emptySet();
        String commaSeparatedAccessParameters = jwt.getClaimAsString(Constants.CLAIM_KEY_ACCESS_PARAMETER);
        if (StringUtils.isNotEmpty(commaSeparatedAccessParameters)) {
            String[] authoritiesArray = commaSeparatedAccessParameters.split(",");
            authorities = Arrays.stream(authoritiesArray)
                    .map(String::trim)
                    .collect(Collectors.toSet());
        }
        user.setAccessParameters(authorities);

        return user;
    }

}
