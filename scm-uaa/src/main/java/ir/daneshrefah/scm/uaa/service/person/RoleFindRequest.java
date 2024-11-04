package ir.daneshrefah.scm.uaa.service.person;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-14
 */
@Data
public class RoleFindRequest extends PagedRequestData {

    private String name;
    private String code;
    private Boolean systemRole;

}
