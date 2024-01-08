package ir.daneshrefah.scm.uaa.client.core;

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
    private ClientAuthenticationType authenticationType;
    private String authenticationValue;
    private ClientAuthenticationType transactionType;
    private String transactionValue;

}
