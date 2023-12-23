package ir.daneshrefah.scm.uaa.security.authenticationProvider;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.type.AuthenticationMethod;
import ir.daneshrefah.scm.uaa.security.token.AbstractAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.FirstLvlStaticAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-23
 */
@AllArgsConstructor
@Getter
public enum AuthenticationTokenTypes {

    LOGIN_STATIC(FirstLvlStaticAuthenticationToken.class, AuthorizationGrantType.FIRST_PASSWORD,
            AuthenticationMethod.STATIC_PASSWORD, false);

    private Class<? extends GeneralAuthenticationToken> tokenClass;
    private AuthorizationGrantType grantType;
    private AuthenticationMethod authenticationMethod;
    private boolean claimCodeProvided;

}
