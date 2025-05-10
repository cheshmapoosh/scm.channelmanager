package ir.daneshrefah.scm.job.model;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.AuditableModel;
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
public class JobDefinition extends AuditableModel<Long> {

    private Long id;
    private String name;
    private String title;
    private JobDefinitionStatus status;
    private JobImplementationType implementationType;
    private String implementationSource;
    private JsonNode payload;
    private String cronExpression;

}
