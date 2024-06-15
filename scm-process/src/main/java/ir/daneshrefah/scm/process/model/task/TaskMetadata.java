package ir.daneshrefah.scm.process.model.task;

import ir.daneshrefah.scm.process.model.BaseProcessModel;
import lombok.Data;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-15
 */
@Data
public class TaskMetadata implements BaseProcessModel {

    private List<String> outputVariables;
    private List<String> actions;
    private String validationSchema;

}
