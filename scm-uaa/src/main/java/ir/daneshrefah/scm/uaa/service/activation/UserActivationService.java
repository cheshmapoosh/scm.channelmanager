package ir.daneshrefah.scm.uaa.service.activation;

import ir.daneshrefah.scm.common.constant.TerminalCodes;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;

public interface UserActivationService {
    void activate(GeneralPerson person, TerminalCodes fromTerminal);
}
