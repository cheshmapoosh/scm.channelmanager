package ir.daneshrefah.scm.common.dto.membership;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import ir.daneshrefah.scm.common.model.person.Nationality;
import ir.daneshrefah.scm.common.model.person.PersonStatus;
import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.common.validation.NotBlankIfPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-28
 */
@Getter
@Setter
public class PersonFindRequest extends PagedRequestData {

    @NotNull
    private PersonType personType;
    @NotNull
    @NotBlank
    private String nationalId;
    @NotBlankIfPresent
    private String subOrganizationId;
    @NotNull
    private Nationality nationality;
    private String username;
    private String firstName;
    private String lastName;
    private PersonStatus active;
    private String branchCode;

}
