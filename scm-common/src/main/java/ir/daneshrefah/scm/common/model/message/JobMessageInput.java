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
public class JobMessageInput extends AbstractInternalMessageInput {

    private final String jobName;
    private final String triggerName;
    private final String jobId;

}
