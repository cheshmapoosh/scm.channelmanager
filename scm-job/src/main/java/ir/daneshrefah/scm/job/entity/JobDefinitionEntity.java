package ir.daneshrefah.scm.job.entity;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.job.model.JobDefinitionStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "TBL_SJB_JOB_DEFINITION")
public class JobDefinitionEntity extends AbstractDefaultEntity<Long> {

    @Id
    private Long id;
    private String name;
    private String title;
    private JobDefinitionStatus status;
    private Service service;
    private JsonNode payload;
    private String cronExpression;

}
