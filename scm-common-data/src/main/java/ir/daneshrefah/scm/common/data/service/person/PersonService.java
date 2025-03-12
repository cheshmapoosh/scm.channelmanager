package ir.daneshrefah.scm.common.data.service.person;

import ir.daneshrefah.scm.common.data.entity.person.ClientPersonEntity;
import ir.daneshrefah.scm.common.dto.membership.PersonFindRequest;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.model.person.ClientPerson;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.model.person.GeneralRealPerson;
import ir.daneshrefah.scm.common.model.person.PersonType;

import java.util.List;
import java.util.Optional;

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

    GeneralPerson findPersonByPersonId(Long id);

    Optional<GeneralPerson> findPersonByUsername(String username);

    GeneralPerson findPersonByNicknameAndTerminalCode(String nickname, String terminalCode);

    GeneralPerson findLocalPerson(PersonFindRequest request);

    GeneralRealPerson findPersonByNationalCode(String nationalCode);

    Optional<GeneralPerson> findPerson(PersonType personType, String nationalId, String subOrg);

    List<ClientPerson> findAllClientPerson(String nationalId);


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
