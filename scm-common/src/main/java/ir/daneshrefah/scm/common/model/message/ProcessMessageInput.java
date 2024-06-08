package ir.daneshrefah.scm.common.model.message;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-08
 */
@SuperBuilder
@Getter
public class ProcessMessageInput extends AbstractInternalMessageInput {

    private final String processDefinitionKey;
    private final String processInstanceId;
    private final String taskName;
    private final String taskId;

}
