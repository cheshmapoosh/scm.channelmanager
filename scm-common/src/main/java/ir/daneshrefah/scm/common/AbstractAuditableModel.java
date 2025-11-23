package ir.daneshrefah.scm.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public abstract class AbstractAuditableModel<T> extends AbstractModel<T> {
    @JsonIgnore
    private Integer creator;
    @JsonIgnore
    private Integer lastEditor;
    @JsonIgnore
    private LocalDateTime createDate;
    @JsonIgnore
    private LocalDateTime lastEditDate;

}
