package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.common.dto.membership.PersonFindRequest;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.model.person.GeneralRealPerson;
import ir.daneshrefah.scm.common.model.person.PersonType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-20
 */
@Service
@ConditionalOnProperty(name = "scm.security.person-service", havingValue = "remote", matchIfMissing = false)
public class PersonServiceRemoteImpl implements PersonService {

    @Override
    public PagedResponseData<GeneralPerson> findPagedPersonList(PersonFindRequest request) {
        return null;
    }

    @Override
    public boolean checkPersonExist(PersonFindRequest request) {
        return false;
    }

    @Override
    public GeneralPerson findPersonByPersonId(Integer id) {
        return null;
    }

    @Override
    public Optional<GeneralPerson> findPersonByPersonUsername(String username) {
        return Optional.empty();
    }

    @Override
    public GeneralPerson findPersonByNicknameAndTerminalCode(String nickname, String terminalCode) {
        return null;
    }

    @Override
    public GeneralPerson findLocalPerson(PersonFindRequest request) {
        return null;
    }

    @Override
    public GeneralRealPerson findPersonByNationalCode(String nationalCode) {
        return null;
    }

    @Override
    public Optional<GeneralPerson> findPerson(PersonType personType,String nationalId, String subOrg) {
        return Optional.empty();
    }
}
