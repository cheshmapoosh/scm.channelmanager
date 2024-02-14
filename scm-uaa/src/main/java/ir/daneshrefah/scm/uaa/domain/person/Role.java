package ir.daneshrefah.scm.uaa.domain.person;

import ir.daneshrefah.scm.common.BaseModel;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-14
 */
@Data
public class Role extends BaseModel<Integer> {

    private String name;
    private String code;
    private String abbreviation;
    private Boolean systemRole;

}
