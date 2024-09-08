package ir.daneshrefah.scm.uaa.client.core;

import ir.daneshrefah.scm.common.model.message.TokenType;
import lombok.Builder;
import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-06
 */
@Builder
@Getter
public class ClientAuthenticationRequest {

    private String username;
    private String terminalCode;
    private String clientId;
    private TokenType tokenType;
    private String authenticationValue;
    private String accessParameter;

}
