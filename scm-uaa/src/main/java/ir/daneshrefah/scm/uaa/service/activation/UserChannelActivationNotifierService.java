package ir.daneshrefah.scm.uaa.service.activation;

import ir.daneshrefah.scm.common.constant.TerminalCodes;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;

public interface UserChannelActivationNotifierService {
    void sendSuccessNotification(GeneralPerson person, String sourceNickname,TerminalCodes sourceChannel,TerminalCodes targetChannel);

    void sendFailedNotification(GeneralPerson person, String sourceNickname,TerminalCodes sourceChannel,TerminalCodes targetChannel);

    void sendRegisteredRequestNotification(GeneralPerson person,String sourceNickname, TerminalCodes sourceChannel,TerminalCodes targetChannel);
}
