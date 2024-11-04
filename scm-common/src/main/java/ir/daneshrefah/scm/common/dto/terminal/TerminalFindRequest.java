package ir.daneshrefah.scm.common.dto.terminal;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import ir.daneshrefah.scm.common.model.terminal.TerminalStatus;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-30
 */
@Data
public class TerminalFindRequest extends PagedRequestData {

    private String code;
    private TerminalStatus status;
    private Boolean supportCheckAuthentication;
    private Boolean supportCheckSecondAuthentication;
    private Boolean supportCheckServiceAccess;
    private Boolean supportCheckAssetAccess;

}
