package ir.daneshrefah.scm.common.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

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

    @Column(name = "CREATOR")
    private String creator;
    @Column(name = "LAST_EDITOR")
    private String lastEditor;
    @Column(name = "CREATE_DATE", insertable = false, updatable = false)
    private LocalTime createDate;
    @Column(name = "LAST_EDIT_DATE", insertable = false)
    private LocalTime lastEditDate;

}
