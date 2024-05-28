package ir.daneshrefah.scm.common.model.recipient;

import ir.daneshrefah.scm.common.model.user.UserIdentifierType;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-19
 */
@Getter
@Builder
public class Recipient implements Serializable {

    /**
     * this property is otp delivery target like cellphone number, email address, ...
     * */
    private final String address;
//    private final AuthenticationLevel authenticationLevel;
    private final String identifier;
    private final UserIdentifierType identifierType;
    private final String terminalCode;
    private final String accessParameter;

}
