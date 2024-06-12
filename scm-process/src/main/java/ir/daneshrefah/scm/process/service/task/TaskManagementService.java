package ir.daneshrefah.scm.process.service.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.process.model.request.TaskRequest;
import ir.daneshrefah.scm.process.model.response.TaskResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskManagementService extends AbstractJavaService{

    @Autowired
    private TaskManagement taskManagement;

    public TaskManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper) {
        super(producerTemplate, objectMapper);
    }

    public List<? extends TaskResponse> getTaskList(TaskRequest taskRequest) throws JsonProcessingException {// Todo remove taskRequest From argument
       return taskManagement.getTaskList(taskRequest);
    }

    public boolean completeTask(TaskRequest taskRequest) throws JsonProcessingException {
        return taskManagement.completeTask(taskRequest);
    }
}
