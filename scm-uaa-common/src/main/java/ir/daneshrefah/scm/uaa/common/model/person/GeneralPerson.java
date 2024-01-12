package ir.daneshrefah.scm.uaa.common.model.person;

import ir.daneshrefah.scm.uaa.common.model.BaseModel;
import ir.daneshrefah.scm.uaa.common.type.Nationality;
import ir.daneshrefah.scm.uaa.common.type.PersonType;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
@Data
public abstract class GeneralPerson extends BaseModel<Integer> {

    private String username;
    private Boolean active;
    private String phone;
    private String mobile;
    private String email;
    private String fax;
    private String address;
    private String postalCode;
    private Nationality nationality;

    public abstract PersonType getType();

}
