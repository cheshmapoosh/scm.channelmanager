package ir.daneshrefah.scm.plugin.api.model.limitation;

import ir.daneshrefah.scm.plugin.api.model.BaseModel;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.service.Service;
import ir.daneshrefah.scm.plugin.api.model.terminal.Channel;
import ir.daneshrefah.scm.plugin.api.model.terminal.Terminal;
import ir.daneshrefah.scm.plugin.api.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.plugin.api.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.plugin.api.type.DurationType;
import ir.daneshrefah.scm.utils.string.StringUtils;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-12
 */
public class ServiceLimitation extends BaseModel<String> {

    private Channel channel;
    private Terminal terminal;
    private Service service;
    private TerminalServiceAccess terminalServiceAccess;
    private TerminalServiceChannelAccess terminalServiceChannelAccess;
    private Object authenticationMethod; // TODO
    private String condition;
    private Pattern conditionPattern;
    private DurationType durationType;
    private Integer duration;
    private Boolean allow;
    private BigDecimal minWithdraw;
    private BigDecimal maxWithdraw;

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public TerminalServiceAccess getTerminalServiceAccess() {
        return terminalServiceAccess;
    }

    public void setTerminalServiceAccess(TerminalServiceAccess terminalServiceAccess) {
        this.terminalServiceAccess = terminalServiceAccess;
    }

    public TerminalServiceChannelAccess getTerminalServiceChannelAccess() {
        return terminalServiceChannelAccess;
    }

    public void setTerminalServiceChannelAccess(TerminalServiceChannelAccess terminalServiceChannelAccess) {
        this.terminalServiceChannelAccess = terminalServiceChannelAccess;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
        if (StringUtils.isNotEmpty(condition)) {
            this.conditionPattern = Pattern.compile(condition);
        }
    }

    public DurationType getDurationType() {
        return durationType;
    }

    public void setDurationType(DurationType durationType) {
        this.durationType = durationType;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Boolean getAllow() {
        return allow;
    }

    public void setAllow(Boolean allow) {
        this.allow = allow;
    }

    public BigDecimal getMinWithdraw() {
        return minWithdraw;
    }

    public void setMinWithdraw(BigDecimal minWithdraw) {
        this.minWithdraw = minWithdraw;
    }

    public BigDecimal getMaxWithdraw() {
        return maxWithdraw;
    }

    public void setMaxWithdraw(BigDecimal maxWithdraw) {
        this.maxWithdraw = maxWithdraw;
    }

    public boolean isAllowServiceCall(TerminalServiceChannelAccess channelAccess, Message message) {
//        Condition:
//        {
//            Terminal:
//            Service:
//            User:
//            {
//                PersonType (INDIVIDUAL_CUSTOMER, EMPLOYEE, CORPORATE_CUSTOMER, SYSTEM)
//                Loyalty
//                Gender
//                Nationality (IRANIAN, FOREIGN)
//                AGE (BIRTH_DATE)
//                AuthenticationMethodFirst
//                AuthenticationMethodSecond
//            }
//        }
        return true;
    }

}
