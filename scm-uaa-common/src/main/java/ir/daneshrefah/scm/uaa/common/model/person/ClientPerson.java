package ir.daneshrefah.scm.uaa.common.model.person;

import ir.daneshrefah.scm.uaa.common.type.PersonType;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-14
 */
public class ClientPerson extends GeneralPerson {

    @Override
    public PersonType getType() {
        return PersonType.CLIENT;
    }
}
