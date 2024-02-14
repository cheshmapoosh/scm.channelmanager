package ir.daneshrefah.scm.common.data.service.person;

import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-24
 */
public interface PersonService {

    PagedResponseData<GeneralPerson> findPagedPersonList(PersonFindRequest request);

    boolean checkPersonExist(PersonFindRequest request);

    GeneralPerson findPersonByPersonId(Integer id);

//    GeneralPerson findPersonInfo(PersonFindRequest request);
//
//    GeneralPerson findCIFPersonInfo(PersonFindRequest request);
//
//
//    GeneralPerson findPersonByPersonProfileId(String id);
//
//    GeneralPerson saveOrUpdateLocalPersonInfoFromCIF(PersonFindRequest request);
//
//    GeneralPerson updatePerson(GeneralPerson person);
//
//    GeneralPerson savePerson(GeneralPerson person);
//
}
