package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.common.data.repository.PersonRepository;
import ir.daneshrefah.scm.common.data.service.person.AbstractPersonServiceDatabaseImpl;
import ir.daneshrefah.scm.common.data.service.person.PersonFindRequest;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-13
 */
@Service
public class PersonServiceDatabaseImpl extends AbstractPersonServiceDatabaseImpl {


    public PersonServiceDatabaseImpl(PersonRepository personRepository) {
        super(personRepository);
    }

    @Override
    public List<GeneralPerson> findCIFPersonInfo(PersonFindRequest request) {
        return null;
    }
}
