package ir.daneshrefah.scm.job.model;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.model.service.Service;
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
public class JobDefinition extends BaseModel<Long> {

    private Long id;
    private String name;
    private String title;
    private JobDefinitionStatus status;
    private Service service;
    private JsonNode payload;
    private String cronExpression;

}
