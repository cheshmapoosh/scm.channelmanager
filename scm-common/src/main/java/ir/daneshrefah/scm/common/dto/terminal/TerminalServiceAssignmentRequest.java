package ir.daneshrefah.scm.common.dto.terminal;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-03
 */
@Data
public class TerminalServiceAssignmentRequest implements RequestData {

    private String terminalId;
    private String serviceId;

}
