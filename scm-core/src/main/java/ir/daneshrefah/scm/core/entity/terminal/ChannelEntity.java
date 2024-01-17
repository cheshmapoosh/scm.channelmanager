package ir.daneshrefah.scm.core.entity.terminal;

import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import ir.daneshrefah.scm.common.data.entity.TerminalEntity;
import ir.daneshrefah.scm.common.model.terminal.ChannelProtocol;
import ir.daneshrefah.scm.core.converter.ChannelProtocolConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "TBL_SCM_CHANNEL")
public class ChannelEntity extends AbstractDefaultEntity<String> {

    @Id
    @Column(name = "CHANNEL_ID")
    private String id;
    private String code;
    private String title;
    @ManyToOne
    @JoinColumn(name = "TERMINAL_ID")
    private TerminalEntity terminal;
    @Convert(converter = ChannelProtocolConverter.class)
    private ChannelProtocol protocol;
    private String channelClassName;
    private String metadata;

}
