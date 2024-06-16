package ir.daneshrefah.scm.common.model.person;


/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-16
 */
public class SmsVerifiedPerson extends GeneralPerson {

    @Override
    public String getTitle() {
        return getMobile1();
    }

    @Override
    public PersonType getPersonType() {
        return PersonType.SMS_VERIFIED;
    }

}
