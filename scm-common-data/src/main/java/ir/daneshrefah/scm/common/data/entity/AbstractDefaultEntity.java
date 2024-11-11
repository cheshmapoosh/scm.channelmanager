package ir.daneshrefah.scm.common.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-10
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AbstractDefaultEntity<T> extends AbstractEntity<T> {

    @Column(name = "CREATOR", updatable = false)
    @CreatedBy
    private String creator;
    @Column(name = "LAST_EDITOR")
    @LastModifiedBy
    private String lastEditor;
    @Column(name = "CREATE_DATE", updatable = false)
    @CreatedDate
    private LocalDateTime createDate;
    @Column(name = "LAST_EDIT_DATE")
    @LastModifiedDate
    private LocalDateTime lastEditDate;

}
