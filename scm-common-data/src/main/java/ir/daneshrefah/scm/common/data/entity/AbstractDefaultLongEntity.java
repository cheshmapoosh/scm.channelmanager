package ir.daneshrefah.scm.common.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-31
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AbstractDefaultLongEntity extends AbstractEntity<Long> {

    @Column(name = "CREATOR", updatable = false)
    private String creator;
    @Column(name = "LAST_EDITOR")
    private String lastEditor;
    @Column(name = "CREATE_DATE", insertable = false, updatable = false)
    private LocalDateTime createDate;
    @Column(name = "LAST_EDIT_DATE", insertable = false)
    private LocalDateTime lastEditDate;

}
