package ir.daneshrefah.scm.job.model;

import ir.daneshrefah.scm.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-23
 */
@Getter
@Setter
public class JobExecution extends BaseModel<Long> {

    private JobDefinition jobDefinition;
    private JobExecutionStatus status;
    private Integer durationMillis;
    private String exceptionClass;
    private String exceptionMessage;
    private String exceptionStackTrace;

}
