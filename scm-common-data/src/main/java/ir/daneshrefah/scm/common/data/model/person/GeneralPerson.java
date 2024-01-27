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
    private Nationality nationality;
    private String mobile1;
    private String mobile2;
    private String mobile3;
    private String phone1;
    private String phone2;
    private String address1;
    private String address2;
    private String address3;
    private String address4;
    private String postalCode1;
    private String postalCode2;
    private String fax;
    private String email;

    public abstract PersonType getType();

}
