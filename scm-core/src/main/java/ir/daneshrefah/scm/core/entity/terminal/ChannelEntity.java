package ir.daneshrefah.scm.core.entity.terminal;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.data.converter.JsonNodeTypeConverter;
import ir.daneshrefah.scm.common.data.entity.AbstractStringAuditableEntity;
import ir.daneshrefah.scm.common.data.entity.terminal.TerminalEntity;
import ir.daneshrefah.scm.common.model.terminal.ChannelProtocol;
import ir.daneshrefah.scm.core.converter.ChannelProtocolConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "TBL_SCM_CHANNEL")
public class ChannelEntity extends AbstractStringAuditableEntity<String> {

    @Id
    @Column(name = "CHANNEL_ID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String code;
    private String title;
    @ManyToOne
    @JoinColumn(name = "TERMINAL_ID")
    private TerminalEntity terminal;
    @Convert(converter = ChannelProtocolConverter.class)
    private ChannelProtocol protocol;
    private String channelClassName;
    @Convert(converter = JsonNodeTypeConverter.class)
    @Column(name = "CHANNEL_METADATA")
    private JsonNode metadata;

}
