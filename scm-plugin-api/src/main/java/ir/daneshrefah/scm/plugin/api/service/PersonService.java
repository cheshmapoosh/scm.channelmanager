package ir.daneshrefah.scm.plugin.api.service;

import ir.daneshrefah.scm.common.data.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.data.type.Nationality;
import ir.daneshrefah.scm.common.data.type.PersonType;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-24
 */
public interface PersonService {

    GeneralPerson findPersonByPersonInfo(PersonType personType, Nationality nationality, String nationalId, String subOrganizationId);

    GeneralPerson findPersonByPersonId(Long id);

    GeneralPerson findPersonByPersonProfileId(String id);

    GeneralPerson defineOrUpdatePersonInfo(PersonType personType, Nationality nationality, String nationalId, String subOrganizationId);

}
