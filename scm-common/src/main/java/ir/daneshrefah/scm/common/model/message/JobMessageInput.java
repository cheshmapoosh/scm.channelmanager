package ir.daneshrefah.scm.common.model.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
public class JobMessageInput extends AbstractInternalMessageInput {

    private String jobName;
    private String triggerName;
    private String jobId;

}
