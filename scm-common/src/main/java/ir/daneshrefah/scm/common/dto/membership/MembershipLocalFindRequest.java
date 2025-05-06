package ir.daneshrefah.scm.common.dto.membership;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
//import ir.daneshrefah.scm.common.model.customer.AssetType;
import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.common.validation.NotBlankIfPresent;
import ir.daneshrefah.scm.common.validation.TerminalCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-02
 */
@Setter
@Getter
public class MembershipLocalFindRequest extends PagedRequestData {

    @NotNull
    private PersonType personType;
    @NotNull
    @NotBlank
    private String nationalId;
    @NotBlankIfPresent
    private String subOrganizationId;
    @TerminalCode
    private String terminal;

}
