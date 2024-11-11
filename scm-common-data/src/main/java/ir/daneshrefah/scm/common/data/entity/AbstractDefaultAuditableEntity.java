package ir.daneshrefah.scm.common.data.entity;

import ir.daneshrefah.scm.common.data.audit.listener.jpa.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(Auditable.class)
@Access(AccessType.FIELD)
public abstract class AbstractDefaultAuditableEntity<T> extends AbstractEntity<T> {

    @Column(name = "CREATOR", updatable = false)
    @CreatedBy
    public abstract <E> E getCreator();

    @Column(name = "LAST_EDITOR")
    @LastModifiedBy
    public abstract <E> E getLastEditor();

    @Column(name = "CREATE_DATE",  updatable = false)
    @CreatedDate
    public abstract LocalDateTime getCreateDate();

    @Column(name = "LAST_EDIT_DATE")
    @LastModifiedDate
    public abstract LocalDateTime getLastEditDate();

}
