package ir.daneshrefah.scm.common.model.person;

import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
@Getter
@Setter
public class EmployeePerson extends GeneralRealPerson {

    private String personnelNo;

    @Override
    public PersonType getPersonType() {
        return PersonType.EMPLOYEE;
    }

}
