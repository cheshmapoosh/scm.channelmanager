package ir.daneshrefah.scm.uaa.common.model.person;

import ir.daneshrefah.scm.uaa.common.type.PersonType;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class SystemPerson extends GeneralPerson {

    @Override
    public PersonType getType() {
        return PersonType.SYSTEM;
    }
}
