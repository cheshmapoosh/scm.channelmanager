package ir.daneshrefah.scm.uaa.common.token;

import ir.daneshrefah.scm.common.model.person.*;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalWebAuthenticationDetails;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static ir.daneshrefah.scm.common.constant.SecurityConstants.USERNAME_ANONYMOUS;
import static ir.daneshrefah.scm.common.constant.SecurityConstants.USERNAME_NONE_PROVIDED;
import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_USERNAME;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-25
 */
@RequiredArgsConstructor
public class JwtTokenConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final String CLAIM_AUTHENTICATION = "claim_authentication";
//    private final Supplier<Authentication> extractAuthentication;

    public UserAuthentication convert(Jwt jwt) {
        return convert(jwt, extractUsername(jwt));
    }

    public UserAuthentication convert(Jwt jwt, String username) {
        String clientId = jwt.getAudience().get(0);

        Collection<GrantedAuthority> authorities = Collections.emptyList();
        String commaSeparatedAuthorities = jwt.getClaimAsString(Constants.CLAIM_KEY_AUTHORITIES);
        commaSeparatedAuthorities = StringUtils.remove(commaSeparatedAuthorities, "[", "]");
        if (StringUtils.isNotEmpty(commaSeparatedAuthorities)) {
            String[] authoritiesArray = commaSeparatedAuthorities.split(",");

            authorities = Arrays.stream(authoritiesArray)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toUnmodifiableList());
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

        String delegatedUsername = USERNAME_NONE_PROVIDED.equalsIgnoreCase(username) ||
                USERNAME_ANONYMOUS.equals(username) || StringUtils.isBlank(username) ? null : username;
        UserAuthentication result = new UserAuthentication(detail,
                user, delegatedUsername, authorities);

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
        if (Objects.nonNull(person)) {
            person.setUsername(jwt.getClaimAsString(Constants.CLAIM_KEY_PERSON_PROFILE_IDENTIFIER));
            if (jwt.hasClaim(Constants.CLAIM_KEY_PERSON_IDENTIFIER)) {
                person.setId(Long.valueOf(jwt.getClaimAsString(Constants.CLAIM_KEY_PERSON_IDENTIFIER)));
            }
            person.setNationality(Nationality.findByCode(jwt.getClaimAsString(Constants.CLAIM_KEY_PERSON_NATIONALITY)));
        }

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
        user.setStatus(UserStatus.ACTIVE);
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

    private String extractUsername(Jwt jwt) {
        if (Objects.isNull(jwt) || Objects.isNull(jwt.getClaim(CLAIM_AUTHENTICATION))) {
            return null;
        }
        Authentication authentication = jwt.getClaim(CLAIM_AUTHENTICATION);
        if (Objects.isNull(authentication.getDetails()) || !(authentication.getDetails() instanceof TerminalWebAuthenticationDetails)) {
            return null;
        }
        TerminalWebAuthenticationDetails details = (TerminalWebAuthenticationDetails) authentication.getDetails();
        return details.getHeader(SCM_PARAMETER_USERNAME);
    }


}
