package ir.daneshrefah.scm.uaa.client.converter.authentication;

import ir.daneshrefah.scm.uaa.client.core.ClientAuthenticationRequest;
import ir.daneshrefah.scm.uaa.client.core.ClientAuthenticationType;
import ir.daneshrefah.scm.uaa.client.provider.token.BaseTerminalAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.provider.token.ClaimAuthenticationToken;
import ir.daneshrefah.scm.utils.constant.Constants;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-08
 */
public class ClaimTokenAuthenticationConverter implements AuthenticationConverter {

    @Override
    public BaseTerminalAuthenticationToken convertByRequest(ClientAuthenticationRequest request) {
        if (null == request || !ClientAuthenticationType.BASIC.equals(request.getTransactionType())) {
            return null;
        }

        if (StringUtils.isEmpty(request.getUsername())) {
            throw new BadCredentialsException("Empty " + Constants.SCM_PARAMETER_USERNAME);
        }

        if (StringUtils.isEmpty(request.getTransactionValue())) {
            throw new BadCredentialsException("Empty " + Constants.SCM_PARAMETER_CLAIM_CODE);
        }

        return new ClaimAuthenticationToken(request.getTerminalCode(), request.getUsername(),
                request.getTransactionValue());
    }

}
