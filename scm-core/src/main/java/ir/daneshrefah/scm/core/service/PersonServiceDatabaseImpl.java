package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.common.data.repository.PersonRepository;
import ir.daneshrefah.scm.common.data.service.person.AbstractPersonServiceDatabaseImpl;
import ir.daneshrefah.scm.common.service.terminal.TerminalService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-13
 */
@Service
@ConditionalOnProperty(name = "scm.security.person-service", havingValue = "local", matchIfMissing = true)
public class PersonServiceDatabaseImpl extends AbstractPersonServiceDatabaseImpl {

    public PersonServiceDatabaseImpl(TerminalService terminalService, PersonRepository personRepository) {
        super(terminalService, personRepository);
    }

}
