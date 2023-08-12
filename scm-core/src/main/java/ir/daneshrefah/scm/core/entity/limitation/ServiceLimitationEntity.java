package ir.daneshrefah.scm.core.entity.limitation;

import ir.daneshrefah.scm.core.converter.DurationTypeConverter;
import ir.daneshrefah.scm.core.entity.AbstractEntity;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.core.entity.terminal.ChannelEntity;
import ir.daneshrefah.scm.core.entity.terminal.TerminalEntity;
import ir.daneshrefah.scm.core.entity.terminal.TerminalServiceAccessEntity;
import ir.daneshrefah.scm.core.entity.terminal.TerminalServiceChannelAccessEntity;
import ir.daneshrefah.scm.plugin.api.type.DurationType;
import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-12
 */
@Entity
@Table(name = "TBL_SCM_SERVICE_LIMITATION")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class ServiceLimitationEntity extends AbstractEntity<String> {

    @Id
    @Column(name = "SERVICE_LIMITATION_ID")
    private String id;
    @ManyToOne
    @JoinColumn(name = "CHANNEL_ID")
    private ChannelEntity channel;
    @ManyToOne
    @JoinColumn(name = "TERMINAL_ID")
    private TerminalEntity terminal;
    @ManyToOne
    @JoinColumn(name = "SERVICE_ID")
    private ServiceEntity service;
    @ManyToOne
    @JoinColumn(name = "TERMINAL_SERVICE_ACCESS_ID")
    private TerminalServiceAccessEntity terminalServiceAccess;
    @ManyToOne
    @JoinColumn(name = "TERMINAL_SERVICE_CHANNEL_ACCESS_ID")
    private TerminalServiceChannelAccessEntity terminalServiceChannelAccess;
    private String condition;
    @Column(name = "DURATION_TYPE_CODE")
    @Convert(converter = DurationTypeConverter.class)
    private DurationType durationType;
    private Integer duration;
    private Boolean allow;
    private BigDecimal minWithdraw;
    private BigDecimal maxWithdraw;


    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public ChannelEntity getChannel() {
        return channel;
    }

    public void setChannel(ChannelEntity channel) {
        this.channel = channel;
    }

    public TerminalEntity getTerminal() {
        return terminal;
    }

    public void setTerminal(TerminalEntity terminal) {
        this.terminal = terminal;
    }

    public ServiceEntity getService() {
        return service;
    }

    public void setService(ServiceEntity service) {
        this.service = service;
    }

    public TerminalServiceAccessEntity getTerminalServiceAccess() {
        return terminalServiceAccess;
    }

    public void setTerminalServiceAccess(TerminalServiceAccessEntity terminalServiceAccess) {
        this.terminalServiceAccess = terminalServiceAccess;
    }

    public TerminalServiceChannelAccessEntity getTerminalServiceChannelAccess() {
        return terminalServiceChannelAccess;
    }

    public void setTerminalServiceChannelAccess(TerminalServiceChannelAccessEntity terminalServiceChannelAccess) {
        this.terminalServiceChannelAccess = terminalServiceChannelAccess;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
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
}
