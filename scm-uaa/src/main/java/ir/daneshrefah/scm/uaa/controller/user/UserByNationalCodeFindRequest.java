package ir.daneshrefah.scm.uaa.controller.user;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.person.PersonType;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2025-01-23
 */
@Data
public class UserByNationalCodeFindRequest implements RequestData {

    private PersonType personType;
    private String nationalId;
    private String subOrganizationId;
    private String terminalCode;

}
