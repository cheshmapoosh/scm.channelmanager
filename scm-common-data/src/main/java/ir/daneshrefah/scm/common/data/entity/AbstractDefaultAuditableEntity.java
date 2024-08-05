package ir.daneshrefah.scm.common.data.entity;

import ir.daneshrefah.scm.common.data.audit.listener.jpa.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
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
public abstract class AbstractDefaultAuditableEntity<T> extends AbstractEntity<T> {

    @Column(name = "CREATOR", updatable = false)
    @CreatedBy
    private String creator;
    @Column(name = "LAST_EDITOR")
    @LastModifiedBy
    private String lastEditor;
    @Column(name = "CREATE_DATE", insertable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createDate;
    @Column(name = "LAST_EDIT_DATE", insertable = false)
    @LastModifiedDate
    private LocalDateTime lastEditDate;

}
