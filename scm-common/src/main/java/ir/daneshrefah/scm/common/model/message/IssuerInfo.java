package ir.daneshrefah.scm.common.model.message;

import ir.daneshrefah.scm.common.model.person.PersonType;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-18
 */
@Getter
@Builder
public class IssuerInfo implements Serializable {

    private final String parentCorrelationId;
    private final PersonType personType;
    private final String personUsername;
    private final String terminalCode;
    private final String accessParameter;
    private final String remoteAddress;
    private final String xForwardedFor;
    private final String hostAddress;
    private final String instanceName;

}
