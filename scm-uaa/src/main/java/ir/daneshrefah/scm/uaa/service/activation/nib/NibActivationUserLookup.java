package ir.daneshrefah.scm.uaa.service.activation.nib;

import ir.daneshrefah.scm.common.constant.TerminalType;

public interface NibActivationUserLookup {
    boolean existsUser(String username, TerminalType terminal);
}
