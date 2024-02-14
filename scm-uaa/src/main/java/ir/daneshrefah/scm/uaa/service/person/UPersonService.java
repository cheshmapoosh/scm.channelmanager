package ir.daneshrefah.scm.uaa.service.person;

import ir.daneshrefah.scm.common.data.service.person.PersonFindRequest;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-13
 */
public interface UPersonService extends PersonService {

    List<GeneralPerson> findCIFPersonInfo(PersonFindRequest request);

    GeneralPerson addPersonInfoFromCIF(PersonFindRequest request);

    GeneralPerson updatePersonInfoFromCIF(PersonFindRequest request);

}
