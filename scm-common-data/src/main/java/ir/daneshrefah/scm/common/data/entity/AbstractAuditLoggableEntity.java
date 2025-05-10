package ir.daneshrefah.scm.common.data.entity;

import ir.daneshrefah.scm.common.data.audit.listener.jpa.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(Auditable.class)
@Access(AccessType.FIELD)
public abstract class AbstractAuditLoggableEntity<T> extends AbstractAuditableEntity<T> {
}
