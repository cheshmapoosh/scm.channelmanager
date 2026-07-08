package ir.daneshrefah.scm.provider.task.autoconfigure;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TaskProviderInstanceProperties {
    private boolean enabled = true;
    private String engineType;

}
