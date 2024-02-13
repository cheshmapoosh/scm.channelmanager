package ir.daneshrefah.scm.common.model.person;


/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-14
 */
public class ClientPerson extends GeneralPerson {

    @Override
    public PersonType getPersonType() {
        return PersonType.CLIENT;
    }

}
