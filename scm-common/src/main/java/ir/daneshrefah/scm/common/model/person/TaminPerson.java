package ir.daneshrefah.scm.common.model.person;

import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-13
 */
@Getter
@Setter
public class TaminPerson extends GeneralLegalPerson {

    @Override
    public PersonType getPersonType() {
        return PersonType.TAMIN;
    }

}
