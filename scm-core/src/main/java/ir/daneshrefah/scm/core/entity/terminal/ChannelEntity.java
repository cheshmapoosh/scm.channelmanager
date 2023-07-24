package ir.daneshrefah.scm.core.entity.terminal;

import ir.daneshrefah.scm.core.entity.AbstractEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "TBL_SCM_CHANNEL")
public class ChannelEntity extends AbstractEntity<String> {
    @Id
    @Column(name = "CHANNEL_ID")
    private String id;
    private String code;
    private String title;
    private String protocolCode;
    private String metadata;

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

    public String getProtocolCode() {
        return protocolCode;
    }

    public void setProtocolCode(String protocol) {
        this.protocolCode = protocol;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
