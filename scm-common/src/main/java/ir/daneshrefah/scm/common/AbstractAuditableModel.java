package ir.daneshrefah.scm.common;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public abstract class AbstractAuditableModel<T> extends AbstractModel<T> {
    private String creator;
    private String lastEditor;
    private LocalDateTime createDate;
    private LocalDateTime lastEditDate;

}
