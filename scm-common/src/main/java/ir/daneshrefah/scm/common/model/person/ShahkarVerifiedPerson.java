package ir.daneshrefah.scm.common.model.person;


import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-16
 */
@Data
public class ShahkarVerifiedPerson extends GeneralPerson {

    private String nationalCode;

    @Override
    public String getTitle() {
        return nationalCode;
    }

    @Override
    public PersonType getPersonType() {
        return PersonType.SHAHKAR_VERIFIED;
    }

}
