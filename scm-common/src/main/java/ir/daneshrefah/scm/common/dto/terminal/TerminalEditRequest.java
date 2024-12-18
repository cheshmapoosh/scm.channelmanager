package ir.daneshrefah.scm.common.dto.terminal;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.terminal.TerminalStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank
    private String id;
    @NotBlank
    private String title;
    @NotNull
    private TerminalStatus status;
    private boolean supportCheckAuthentication;
    private boolean supportCheckSecondAuthentication;
    private boolean supportCheckServiceAccess;
    private boolean supportCheckAssetAccess;
    private boolean supportCustomerInjection;
    @NotNull
    private LocalDateTime lastEditDate;
}
