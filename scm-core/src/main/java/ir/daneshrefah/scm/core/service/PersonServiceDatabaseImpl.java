package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.plugin.api.service.PersonService;
import ir.daneshrefah.scm.common.data.model.person.GeneralPerson;
import org.springframework.stereotype.Service;


/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-24
 */
@Service
public class PersonServiceDatabaseImpl implements PersonService {

    @Override
    public GeneralPerson findPersonByPersonId(Long id) {
        return null;
    }

    @Override
    public GeneralPerson findPersonByPersonProfileId(String id) {
        return null;
    }

}
