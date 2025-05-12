package ir.daneshrefah.scm.common.dto.asset;

import ir.daneshrefah.scm.common.constant.AssignmentType;
import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.common.validation.NotBlankIfPresent;
import ir.daneshrefah.scm.common.validation.TerminalCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MembershipTerminalServiceAssignmentRequest implements RequestData {
    @TerminalCode
    @NotNull
    @NotBlank
    private String terminalCode;
    @NotNull
    private PersonType personType;
    @NotNull
    @NotBlank
    private String nationalId;
    @NotBlankIfPresent
    private String subOrganizationId;
    @NotNull
    @NotBlank
    private String accountNumber;
    @NotNull
    private List<Long> channelServiceAccessIdList;
    @NotNull
    private AssignmentType assignmentType;

}
