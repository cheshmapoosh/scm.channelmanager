package ir.daneshrefah.scm.common.service.terminal;

import ir.daneshrefah.scm.common.dto.RequestData;
import ir.daneshrefah.scm.common.model.terminal.TerminalStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-03
 */
@Data
public class TerminalEditRequest implements RequestData {

    private String id;
    private String code;
    private String title;
    private Long legacyTerminalId;
    private TerminalStatus status;
    private boolean supportCheckAuthentication;
    private boolean supportCheckSecondAuthentication;
    private boolean supportCheckServiceAccess;
    private boolean supportCheckAssetAccess;
    private LocalDateTime lastEditDate;

}
