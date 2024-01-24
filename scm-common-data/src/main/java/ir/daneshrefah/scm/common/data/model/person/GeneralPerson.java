package ir.daneshrefah.scm.common.data.model.person;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.data.type.Nationality;
import ir.daneshrefah.scm.common.data.type.PersonType;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
@Data
public abstract class GeneralPerson extends BaseModel<Long> {

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
