package ir.daneshrefah.scm.entity.terminal;

import ir.daneshrefah.scm.common.model.service.ServiceType;
import ir.daneshrefah.scm.common.model.terminal.Protocol;
import ir.daneshrefah.scm.entity.AbstractEntity;
import ir.daneshrefah.scm.repository.converter.ProtocolConverter;
import ir.daneshrefah.scm.repository.converter.ServiceTypeConverter;
import jakarta.persistence.*;

@Entity
@Table(name = "TBL_SCM_CHANNEL")
public class ChannelEntity extends AbstractEntity {
    @Id
    @Column(name = "CHANNEL_ID")
    private String id;
    private String code;
    private String title;
    @Column(name = "PROTOCOL_CODE")
    @Convert(converter = ProtocolConverter.class)
    private Protocol protocol;
    private String restProtocolContext;
    private Integer restProtocolPort;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public String getRestProtocolContext() {
        return restProtocolContext;
    }

    public void setRestProtocolContext(String restProtocolContext) {
        this.restProtocolContext = restProtocolContext;
    }

    public Integer getRestProtocolPort() {
        return restProtocolPort;
    }

    public void setRestProtocolPort(Integer restProtocolPort) {
        this.restProtocolPort = restProtocolPort;
    }
}
