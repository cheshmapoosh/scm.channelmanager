package ir.daneshrefah.scm.uaa.client.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-06
 */
@AllArgsConstructor
@Getter
public class ClientAuthenticationRequest {

    private ClientAuthenticationType type;
    private String username;
    private String terminalCode;
    private String value;

}
