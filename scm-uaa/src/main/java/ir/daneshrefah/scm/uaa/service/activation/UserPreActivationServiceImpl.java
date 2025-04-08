package ir.daneshrefah.scm.uaa.service.activation;

import ir.daneshrefah.scm.common.data.service.person.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserPreActivationServiceImpl implements UserPreActivationService {

    private final PersonService personService;
    private final UserActivationService userActivationService;

    @Override
    public void prepare(String username, String fromTerminal) {
        //TODO
    }
}
