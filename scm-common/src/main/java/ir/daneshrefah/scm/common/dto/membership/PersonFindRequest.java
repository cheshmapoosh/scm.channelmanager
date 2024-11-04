package ir.daneshrefah.scm.common.dto.membership;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import ir.daneshrefah.scm.common.model.person.Nationality;
import ir.daneshrefah.scm.common.model.person.PersonType;
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

    private PersonType personType;
    private String nationalId;
    private String subOrganizationId;
    private Nationality nationality;
    private String username;
    private String firstName;
    private String lastName;
    private Boolean active;
    private String branchCode;





}
