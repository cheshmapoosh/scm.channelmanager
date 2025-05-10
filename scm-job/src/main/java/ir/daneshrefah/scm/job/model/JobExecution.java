package ir.daneshrefah.scm.job.model;

import ir.daneshrefah.scm.common.AuditableModel;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-23
 */
@Getter
@Setter
public class JobExecution extends AuditableModel<Long> {

    private JobDefinition jobDefinition;
    private JobExecutionStatus status;
    private Instant startTime;
    private Instant endTime;
    private Integer durationMillis;
    private String exceptionClass;
    private String exceptionMessage;
    private String exceptionStackTrace;

}
