package ir.daneshrefah.scm.process.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-24
 */
@RequiredArgsConstructor
@Service
public class ProcessDefinitionService {

    private final ProcessEngine processEngine;

    @SneakyThrows
    public void deployProcessDefinition() {
        String filePath = "C:\\Users\\A\\Desktop\\diagram_1.bpmn";
        String bpmn = FileUtils.readFileToString(new File(filePath));
        String processKey = "withdrawal_request";
        Map<String, Object> variables = new HashMap<>();
        variables.put("signerList", List.of("reza", "ali", "hasan"));
        List<ProcessDefinition> definitions = processEngine.getRepositoryService().createProcessDefinitionQuery()
//                .processDefinitionKey(processKey)
//                .latestVersion()
                .list();
        for (Iterator<ProcessDefinition> iterator = definitions.iterator(); iterator.hasNext(); ) {
            ProcessDefinition definition = iterator.next();
            processEngine.getRepositoryService().deleteDeployment(definition.getDeploymentId(), true);
        }

        processEngine.getRepositoryService()
                .createDeployment()
                .addString(processKey + ".bpmn", bpmn)
                .deploy();

        processEngine.getRuntimeService().startProcessInstanceByKey(processKey, variables);

        List<Task> taskList = processEngine.getTaskService().createTaskQuery()
                .active() // Filter for active tasks only (optional)
                .or()
                .taskAssigneeIn("reza", "ali")
                .endOr()
                .list();
    }
}
