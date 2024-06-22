package ir.daneshrefah.scm.common.model.person;


/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-00-18
 */
public class UnknownPerson extends GeneralPerson {

    @Override
    public String getTitle() {
        return getUsername();
    }

    @Override
    public PersonType getPersonType() {
        return PersonType.UNKNOWN;
    }

}
