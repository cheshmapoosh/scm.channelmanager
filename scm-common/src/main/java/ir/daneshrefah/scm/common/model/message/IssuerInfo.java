package ir.daneshrefah.scm.common.model.message;

import ir.daneshrefah.scm.common.model.person.PersonType;
import lombok.Builder;
import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-18
 */
@Getter
@Builder
public class IssuerInfo {

    private String parentCorrelationId;
    private String nickname;
    private PersonType personType;
    private String username;
    private String terminalCode;
    private String hostAddress;
    private String instanceName;

}
