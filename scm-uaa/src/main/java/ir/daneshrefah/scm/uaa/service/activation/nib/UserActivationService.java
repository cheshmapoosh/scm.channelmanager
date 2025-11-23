package ir.daneshrefah.scm.uaa.service.activation.nib;

import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;

public interface UserActivationService {
    void activate(GeneralPerson person, TerminalType fromTerminal);
}
