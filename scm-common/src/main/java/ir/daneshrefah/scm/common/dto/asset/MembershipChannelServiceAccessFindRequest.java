package ir.daneshrefah.scm.common.dto.asset;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.validation.TerminalCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MembershipChannelServiceAccessFindRequest implements RequestData {
    @TerminalCode
    @NotNull
    @NotBlank
    private String terminalCode;
    @NotNull
    @NotBlank
    private String accountNumber;
}
