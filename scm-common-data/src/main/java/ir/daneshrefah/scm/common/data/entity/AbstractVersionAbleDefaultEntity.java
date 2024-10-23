package ir.daneshrefah.scm.common.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Version;
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
public abstract class AbstractVersionAbleDefaultEntity<T> extends AbstractEntity<T> {

    @Column(name = "CREATOR", updatable = false)
    @CreatedBy
    private String creator;
    @Column(name = "LAST_EDITOR")
    @LastModifiedBy
    private String lastEditor;
    @Column(name = "CREATE_DATE", insertable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createDate;
    @Column(name = "LAST_EDIT_DATE")
    @Version
    @LastModifiedDate
    private LocalDateTime lastEditDate;

}
