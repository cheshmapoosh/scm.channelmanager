package ir.daneshrefah.scm.uaa.service.activation;

import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;

public interface UserChannelActivationNotifierService {
    void sendSuccessNotification(GeneralPerson person, String sourceNickname, TerminalType sourceChannel, TerminalType targetChannel);

    void sendFailedNotification(GeneralPerson person, String sourceNickname,TerminalType sourceChannel,TerminalType targetChannel);

    void sendRegisteredRequestNotification(GeneralPerson person,String sourceNickname, TerminalType sourceChannel,TerminalType targetChannel);
}
