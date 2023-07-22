package ir.daneshrefah.scm.entity.terminal;

import ir.daneshrefah.scm.entity.AbstractEntity;
import ir.daneshrefah.scm.entity.service.ServiceEntity;
import jakarta.persistence.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-22
 */
@Entity
@Table(name = "TBL_SCM_TERMINAL_SERVICE_CHANNEL_ACCESS")
public class TerminalServiceChannelAccessEntity extends AbstractEntity {

    @Id
    @Column(name = "TERMINAL_SERVICE_CHANNEL_ACCESS_ID")
    private String id;
    @ManyToOne
    @JoinColumn(name = "terminal_service_access_id")
    private TerminalServiceAccessEntity terminalServiceAccessEntity;
    @ManyToOne
    @JoinColumn(name = "channel_id")
    private ChannelEntity channelEntity;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public TerminalServiceAccessEntity getTerminalServiceAccessEntity() {
        return terminalServiceAccessEntity;
    }

    public void setTerminalServiceAccessEntity(TerminalServiceAccessEntity terminalServiceAccessEntity) {
        this.terminalServiceAccessEntity = terminalServiceAccessEntity;
    }

    public ChannelEntity getChannelEntity() {
        return channelEntity;
    }

    public void setChannelEntity(ChannelEntity channelEntity) {
        this.channelEntity = channelEntity;
    }
}
