package ir.daneshrefah.scm.common.dto.membership;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class MembershipChannelAccessAssignmentRequest extends CustomerFindRequest{
    @NotNull
    @NotBlank
    private String terminalCode;
    @NotNull
    private List<String> accountNumbers;
}
