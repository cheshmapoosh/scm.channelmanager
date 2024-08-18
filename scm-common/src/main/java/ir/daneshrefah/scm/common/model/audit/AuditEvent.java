package ir.daneshrefah.scm.common.model.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ir.daneshrefah.scm.common.model.audit.constants.RevisionType;
import ir.daneshrefah.scm.common.model.event.Event;
import ir.daneshrefah.scm.common.model.event.constants.EventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.sql.Timestamp;


@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
public class AuditEvent extends Event {
    @JsonIgnore
    private Class<?> classType;
    private String classTypeStr;
    private MetaData metaData;
    private Object instanceId;
    private Object data;
    private RevisionType revisionType;
    private String creator;
    private String modifyBy;
    private Timestamp timestamp;
    @Override
    public EventType getEventType() {
        return EventType.AUDIT;
    }
}
