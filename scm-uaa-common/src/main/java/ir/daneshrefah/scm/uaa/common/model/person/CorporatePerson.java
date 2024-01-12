package ir.daneshrefah.scm.uaa.common.model.person;

import ir.daneshrefah.scm.uaa.common.type.PersonType;

import java.time.LocalDate;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class CorporatePerson extends GeneralPerson {

    private String nationalId;
    private String subOrganizationId;
    private LocalDate registerDate;

    @Override
    public PersonType getType() {
        return PersonType.CORPORATE_CUSTOMER;
    }

}
