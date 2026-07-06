package ir.daneshrefah.scm.provider.task.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessInstanceUpdateRequest {
    private Long id;
    private String description;
}
