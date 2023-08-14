package ir.daneshrefah.scm.common.model.authority;


import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.model.authentication.AuthenticationMethod;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;

import java.util.regex.Pattern;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public abstract class Authority extends BaseModel {

    private Terminal sourceTerminal;
    private Channel sourceChannel;
    private Service sourceService;
    private AuthenticationMethod sourceAuthenticationMethod;
    private String sourceCondition;
    private Pattern sourceConditionPattern;
    private String sourceUser;
    private String sourceMembership;

    public abstract AuthorityType getAuthorityType();

    public Terminal getSourceTerminal() {
        return sourceTerminal;
    }

    public void setSourceTerminal(Terminal sourceTerminal) {
        this.sourceTerminal = sourceTerminal;
    }

    public Channel getSourceChannel() {
        return sourceChannel;
    }

    public void setSourceChannel(Channel sourceChannel) {
        this.sourceChannel = sourceChannel;
    }

    public Service getSourceService() {
        return sourceService;
    }

    public void setSourceService(Service sourceService) {
        this.sourceService = sourceService;
    }

    public AuthenticationMethod getSourceAuthenticationMethod() {
        return sourceAuthenticationMethod;
    }

    public void setSourceAuthenticationMethod(AuthenticationMethod sourceAuthenticationMethod) {
        this.sourceAuthenticationMethod = sourceAuthenticationMethod;
    }

    public String getSourceCondition() {
        return sourceCondition;
    }

    public void setSourceCondition(String sourceCondition) {
        this.sourceCondition = sourceCondition;
        if (null != sourceCondition) {
            sourceConditionPattern = Pattern.compile(sourceCondition);
        }
    }

    public String getSourceUser() {
        return sourceUser;
    }

    public void setSourceUser(String sourceUser) {
        this.sourceUser = sourceUser;
    }

    public String getSourceMembership() {
        return sourceMembership;
    }

    public void setSourceMembership(String sourceMembership) {
        this.sourceMembership = sourceMembership;
    }
}
