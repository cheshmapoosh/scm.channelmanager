package ir.daneshrefah.scm.uaa.service.person;

import ir.daneshrefah.scm.common.dto.membership.PersonFindRequest;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.model.person.DiffGeneralPerson;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.uaa.domain.role.Role;

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

    List<Role> findPersonRoleList(Integer personId);

    Role addPersonRole(Integer personId, Integer roleId);

    Role addPersonRole(Integer personId, String roleCode);

    GeneralPerson syncPersonInfoFromCIF(PersonFindRequest request);
    GeneralPerson syncPersonInfoFromCIF(Integer personId);

    DiffGeneralPerson diffPersonInfoFromCIFAndLocal(Integer personId);
}
