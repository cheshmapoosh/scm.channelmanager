package ir.daneshrefah.scm.common.model.recipient;

import ir.daneshrefah.scm.common.model.user.UserIdentifierType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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
@Setter
public class Recipient implements Serializable {

    /**
     * this property is otp delivery target like cellphone number, email address, ...
     */
    private String address;
    //    private final AuthenticationLevel authenticationLevel;
    private String identifier;
    private UserIdentifierType identifierType;
    private String terminalCode;
    private String accessParameter;

    public Recipient() {
    }

    public Recipient(String address, String identifier, UserIdentifierType identifierType, String terminalCode, String accessParameter) {
        this.address = address;
        this.identifier = identifier;
        this.identifierType = identifierType;
        this.terminalCode = terminalCode;
        this.accessParameter = accessParameter;
    }
}
