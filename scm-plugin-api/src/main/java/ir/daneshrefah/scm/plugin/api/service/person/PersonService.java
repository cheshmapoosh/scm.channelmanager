package ir.daneshrefah.scm.plugin.api.service.person;

import ir.daneshrefah.scm.common.data.model.person.GeneralPerson;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-24
 */
public interface PersonService {

    GeneralPerson findPersonInfo(PersonInfoRequest request);

    GeneralPerson findCIFPersonInfo(PersonInfoRequest request);

    GeneralPerson findPersonByPersonId(Long id);

    GeneralPerson findPersonByPersonProfileId(String id);

    GeneralPerson saveOrUpdateLocalPersonInfoFromCIF(PersonInfoRequest request);

}
