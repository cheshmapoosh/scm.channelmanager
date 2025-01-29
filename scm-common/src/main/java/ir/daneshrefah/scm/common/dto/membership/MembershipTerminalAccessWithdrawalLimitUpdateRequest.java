package ir.daneshrefah.scm.common.dto.membership;

import ir.daneshrefah.scm.common.validation.NotBlankIfPresent;
import ir.daneshrefah.scm.common.validation.Numeric;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class MembershipTerminalAccessWithdrawalLimitUpdateRequest extends CustomerFindRequest {

    @NotBlankIfPresent
    private String maxWithdrawalPerDay;
    @NotBlankIfPresent
    private String maxWithdrawalPerMonth;
    @NotNull
    @NotBlank
    private String terminalCode;
    @NotNull
    @NotBlank
    @Numeric
    private String accountNumber;

}
