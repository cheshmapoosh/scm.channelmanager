package ir.daneshrefah.scm.uaa.domain.role;

import ir.daneshrefah.scm.common.AbstractAuditableModel;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-14
 */
@Data
public class Role extends AbstractAuditableModel<Integer> {

    private String name;
    private String code;
    private String abbreviation;
    private Boolean systemRole;

}
