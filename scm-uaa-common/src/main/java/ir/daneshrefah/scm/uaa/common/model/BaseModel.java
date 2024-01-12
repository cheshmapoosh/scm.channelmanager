package ir.daneshrefah.scm.uaa.common.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
@Getter
@Setter
public class BaseModel<T> implements Serializable {

    private T id;
    private String creator;
    private String lastEditor;
    private LocalDateTime createDate;
    private LocalDateTime lastEditDate;

}
