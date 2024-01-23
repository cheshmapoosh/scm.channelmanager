package ir.daneshrefah.scm.job.entity;

import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import ir.daneshrefah.scm.job.model.JobExecutionStatus;
import jakarta.persistence.*;
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
@Entity
@Table(name = "TBL_SJB_JOB_EXECUTION")
public class JobExecutionEntity extends AbstractDefaultEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private JobDefinitionEntity jobDefinition;
    private JobExecutionStatus status;
    private Integer durationMillis;
    private String exceptionClass;
    private String exceptionMessage;
    private String exceptionStackTrace;

}
