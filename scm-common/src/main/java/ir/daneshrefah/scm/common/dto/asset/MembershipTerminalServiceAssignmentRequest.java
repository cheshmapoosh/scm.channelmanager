package ir.daneshrefah.scm.common.dto.asset;

import ir.daneshrefah.scm.common.constant.AssignmentType;
import ir.daneshrefah.scm.common.dto.spec.RequestData;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MembershipTerminalServiceAssignmentRequest implements RequestData {
    @NotNull
    private Long mcsaId;
    @NotNull
    private List<Long> serviceIdList;
    @NotNull
    private AssignmentType assignmentType;

}
