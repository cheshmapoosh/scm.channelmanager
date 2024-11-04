package ir.daneshrefah.scm.common.dto.terminal;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
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
    private TerminalStatus status;
    private Boolean supportCheckAuthentication;
    private Boolean supportCheckSecondAuthentication;
    private Boolean supportCheckServiceAccess;
    private Boolean supportCheckAssetAccess;
    private Boolean supportCustomerInjection;
    private LocalDateTime lastEditDate;
}
