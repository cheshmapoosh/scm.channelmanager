package ir.daneshrefah.scm.plugin.api.service;

import ir.daneshrefah.scm.common.data.model.person.GeneralPerson;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-24
 */
public interface PersonService {

    public GeneralPerson findPersonByPersonId(Long id);

    public GeneralPerson findPersonByPersonProfileId(String id);

}
