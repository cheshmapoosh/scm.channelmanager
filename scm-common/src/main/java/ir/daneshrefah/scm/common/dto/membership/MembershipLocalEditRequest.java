package ir.daneshrefah.scm.common.dto.membership;

import ir.daneshrefah.scm.common.dto.spec.ResponseData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MembershipLocalEditRequest implements ResponseData {
    @NotNull
    private Long id;
    @NotNull
    private Boolean active;
    @NotNull
    @NotBlank
    private String maxWithdrawalPerDay;
    @NotNull
    @NotBlank
    private String maxPersWithdrawalPerDay;
    private String reason;
    private String userReason;
}
