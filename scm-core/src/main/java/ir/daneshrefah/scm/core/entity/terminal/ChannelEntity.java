package ir.daneshrefah.scm.core.entity.terminal;

import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "TBL_SCM_CHANNEL")
public class ChannelEntity extends AbstractDefaultEntity<String> {
    @Id
    @Column(name = "CHANNEL_ID")
    private String id;
    private String code;
    private String title;
    private String channelClassName;
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

    public String getChannelClassName() {
        return channelClassName;
    }

    public void setChannelClassName(String protocol) {
        this.channelClassName = protocol;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
