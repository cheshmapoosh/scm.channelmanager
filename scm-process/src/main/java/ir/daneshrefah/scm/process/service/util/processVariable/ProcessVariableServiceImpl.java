package ir.daneshrefah.scm.process.service.util.processVariable;

import lombok.AllArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@AllArgsConstructor
public class ProcessVariableServiceImpl implements ProcessVariableService {
    private final RuntimeService runtimeService;

    @Override
    public Map<String, Object> getProcessVariables(String processInstanceId) {
        return runtimeService.getVariables(processInstanceId);
    }

    @Override
    public void setProcessVariables(String processInstanceId ,String variableName,Object variableValue) {
          runtimeService.setVariable(processInstanceId, variableName, variableValue);
    }

    public void setProcessVariables(String processInstanceId, Map<String, Object> variables) {
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            runtimeService.setVariable(processInstanceId, entry.getKey(),entry.getValue());
        }
    }
}
