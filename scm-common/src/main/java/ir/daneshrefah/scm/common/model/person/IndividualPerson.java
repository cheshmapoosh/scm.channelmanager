package ir.daneshrefah.scm.common.model.person;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class IndividualPerson extends GeneralRealPerson {

    @Override
    public PersonType getPersonType() {
        return PersonType.REAL;
    }

}
