package ir.daneshrefah.scm.uaa.service.activation;

import ir.daneshrefah.scm.common.constant.TerminalCodes;
import ir.daneshrefah.scm.common.data.repository.PersonRepository;
import ir.daneshrefah.scm.common.data.repository.assets.MembershipRepository;
import ir.daneshrefah.scm.common.data.repository.assets.MembershipTerminalAccessRepository;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.uaa.repository.authentication.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NibUserActivationServiceImpl implements UserActivationService {

    private final MembershipTerminalAccessRepository membershipTerminalAccessRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final PersonRepository personRepository;

    @Override
    public void activate(GeneralPerson person, TerminalCodes fromTerminal) {

        //TODO MOHAMMAD IMPLEMENTATION
        /*
           - 'person' contains GeneralLegalPerson or GeneralRealPerson instance, depends on 'fromTerminal'
           - 'fromTerminal' contains 'IB'  for real and 'CIB' for legal person.
         */

    }
}
