package ir.daneshrefah.scm.common.service;

import ir.daneshrefah.scm.common.dto.RequestData;
import ir.daneshrefah.scm.common.model.terminal.TerminalStatus;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-02
 */
@Data
public class TerminalInfoRequest implements RequestData {

    private String terminalId;
    private String code;
    private String title;
    private Long legacyTerminalId;
    private TerminalStatus status;
    private boolean supportCheckAuthentication;
    private boolean supportCheckSecondAuthentication;
    private boolean supportCheckServiceAccess;
    private boolean supportCheckAssetAccess;

}
